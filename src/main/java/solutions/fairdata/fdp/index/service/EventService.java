/**
 * The MIT License
 * Copyright © 2020 https://fairdata.solutions
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package solutions.fairdata.fdp.index.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.eclipse.rdf4j.util.iterators.EmptyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import solutions.fairdata.fdp.index.api.dto.PingDTO;
import solutions.fairdata.fdp.index.database.repository.EventRepository;
import solutions.fairdata.fdp.index.database.repository.IndexEntryRepository;
import solutions.fairdata.fdp.index.entity.IndexEntry;
import solutions.fairdata.fdp.index.entity.IndexEntryState;
import solutions.fairdata.fdp.index.entity.config.EventsConfig;
import solutions.fairdata.fdp.index.entity.events.Event;
import solutions.fairdata.fdp.index.entity.events.EventType;
import solutions.fairdata.fdp.index.entity.http.Exchange;
import solutions.fairdata.fdp.index.entity.http.ExchangeState;
import solutions.fairdata.fdp.index.exceptions.IncorrectPingFormatException;
import solutions.fairdata.fdp.index.exceptions.NotFoundException;
import solutions.fairdata.fdp.index.exceptions.RateLimitException;
import solutions.fairdata.fdp.index.utils.AdminTriggerUtils;
import solutions.fairdata.fdp.index.utils.IncomingPingUtils;
import solutions.fairdata.fdp.index.utils.MetadataRetrievalUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;

@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private IndexEntryRepository indexEntryRepository;

    @Autowired
    private IndexEntryService indexEntryService;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private EventsConfig eventsConfig;

    public Iterable<Event> getEvents(IndexEntry indexEntry) {
        // TODO: make events pagination in the future
        return eventRepository.getAllByRelatedTo(indexEntry, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created")));
    }

    public Iterable<Event> getEvents(String clientUrl) {
        return indexEntryService.findEntry(clientUrl).map(this::getEvents).orElse(EmptyIterator::new);
    }

    @SneakyThrows
    public Event acceptIncomingPing(HttpEntity<String> httpEntity, HttpServletRequest request) {
        var remoteAddr = request.getRemoteAddr();
        var rateLimitSince = Instant.now().minus(eventsConfig.getPingRateLimitDuration());
        var previousPings = eventRepository.findAllByIncomingPingExchangeRemoteAddrAndCreatedAfter(remoteAddr, rateLimitSince);
        if (previousPings.size() > eventsConfig.getPingRateLimitHits()) {
            logger.warn("Rate limit for PING reached by: " + remoteAddr);
            throw new RateLimitException(String.format(
                    "Rate limit reached for %s (max. %d per %s) - PING ignored",
                    remoteAddr, eventsConfig.getPingRateLimitHits(), eventsConfig.getPingRateLimitDuration().toString())
            );
        }

        var event = IncomingPingUtils.prepareEvent(httpEntity, request);
        eventRepository.save(event);
        event.execute();
        try {
            var pingDTO = objectMapper.readValue(httpEntity.getBody(), PingDTO.class);
            var indexEntry = indexEntryService.storeEntry(pingDTO);
            event.getIncomingPing().setNewEntry(indexEntry.getRegistrationTime().equals(indexEntry.getModificationTime()));
            event.getIncomingPing().getExchange().getResponse().setCode(204);
            event.setRelatedTo(indexEntry);
            logger.info("Accepted incoming ping as a new event");
        } catch (Exception e) {
            var ex = new IncorrectPingFormatException("Could not parse PING: " + e.getMessage());
            event.getIncomingPing().getExchange().getResponse().setCode(400);
            event.getIncomingPing().getExchange().getResponse().setBody(objectMapper.writeValueAsString(ex.getErrorDTO()));
            event.setFinished(Instant.now());
            eventRepository.save(event);
            logger.info("Incoming ping has incorrect format: " + e.getMessage());
            throw ex;
        }
        event.setFinished(Instant.now());
        return eventRepository.save(event);
    }

    private void processMetadataRetrieval(Event event) {
        String clientUrl = event.getRelatedTo().getClientUrl();
        if (MetadataRetrievalUtils.shouldRetrieve(event, eventsConfig.getRetrievalRateLimitWait())) {
            indexEntryRepository.save(event.getRelatedTo());
            eventRepository.save(event);
            event.execute();

            logger.info("Retrieving metadata for " + clientUrl);
            MetadataRetrievalUtils.retrieveRepositoryMetadata(event, eventsConfig.getRetrievalTimeout());
            Exchange ex = event.getMetadataRetrieval().getExchange();
            if (ex.getState() == ExchangeState.Retrieved) {
                try {
                    logger.info("Parsing metadata for " + clientUrl);
                    var metadata = MetadataRetrievalUtils.parseRepositoryMetadata(ex.getResponse().getBody());
                    if (metadata.isPresent()) {
                        event.getMetadataRetrieval().setMetadata(metadata.get());
                        event.getRelatedTo().setCurrentMetadata(metadata.get());
                        event.getRelatedTo().setState(IndexEntryState.Valid);
                        logger.info("Storing metadata for " + clientUrl);
                        indexEntryRepository.save(event.getRelatedTo());
                    } else {
                        logger.info("Repository not found in metadata for " + clientUrl);
                        event.getRelatedTo().setState(IndexEntryState.Invalid);
                        event.getMetadataRetrieval().setError("Repository not found in metadata");
                    }
                } catch (Exception e) {
                    logger.info("Cannot parse metadata for " + clientUrl);
                    event.getRelatedTo().setState(IndexEntryState.Invalid);
                    event.getMetadataRetrieval().setError("Cannot parse metadata");
                }
            } else {
                event.getRelatedTo().setState(IndexEntryState.Unreachable);
                logger.info("Cannot retrieve metadata for " + clientUrl + ": " + ex.getError());
            }
        } else {
            logger.info("Rate limit reached for " + clientUrl + " (skipping metadata retrieval)");
            event.getMetadataRetrieval().setError("Rate limit reached (skipping)");
        }
        event.getRelatedTo().setLastRetrievalTime(Instant.now());
        event.finish();
        event = eventRepository.save(event);
        indexEntryRepository.save(event.getRelatedTo());
        webhookService.triggerWebhooks(event);
    }

    @Async
    public void triggerMetadataRetrieval(Event triggerEvent) {
        logger.info("Initiating metadata retrieval triggered by " + triggerEvent.getUuid());
        Iterable<Event> events = MetadataRetrievalUtils.prepareEvents(triggerEvent, indexEntryService);
        for (Event event: events) {
            logger.info("Triggering metadata retrieval for " + event.getRelatedTo().getClientUrl() + " as " + event.getUuid());
            try {
                processMetadataRetrieval(event);
            } catch (Exception e) {
                logger.error("Failed to retrieve metadata: " + e.getMessage());
            }
        }
        logger.info("Finished metadata retrieval triggered by " + triggerEvent.getUuid());
    }

    private void resumeUnfinishedEvents() {
        logger.info("Resuming unfinished events");
        for (Event event : eventRepository.getAllByFinishedIsNull()) {
            logger.info("Resuming event " + event.getUuid());

            try {
                if (event.getType() == EventType.MetadataRetrieval) {
                    processMetadataRetrieval(event);
                } else if (event.getType() == EventType.WebhookTrigger) {
                    webhookService.processWebhookTrigger(event);
                } else {
                    logger.warn("Unknown event type " + event.getUuid() + " (" + event.getType() + ")");
                }
            } catch (Exception e) {
                logger.error("Failed to resume event " + event.getUuid() + ": " + e.getMessage());
            }
        }
        logger.info("Finished unfinished events");
    }

    @PostConstruct
    public void startResumeUnfinishedEvents() {
        executor.submit(this::resumeUnfinishedEvents);
    }

    public Event acceptAdminTrigger(HttpServletRequest request, String clientUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Event event = AdminTriggerUtils.prepareEvent(request, authentication, clientUrl);
        if (clientUrl != null) {
            Optional<IndexEntry> entry = indexEntryService.findEntry(clientUrl);
            if (entry.isEmpty()) {
                throw new NotFoundException("There is no such entry: " + clientUrl);
            }
            event.setRelatedTo(entry.get());
        }
        event.finish();
        return eventRepository.save(event);
    }
}

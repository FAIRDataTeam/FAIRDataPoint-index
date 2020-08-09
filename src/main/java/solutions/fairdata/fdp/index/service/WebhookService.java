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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import solutions.fairdata.fdp.index.api.dto.WebhookPayloadDTO;
import solutions.fairdata.fdp.index.database.repository.EventRepository;
import solutions.fairdata.fdp.index.database.repository.WebhookRepository;
import solutions.fairdata.fdp.index.entity.config.EventsConfig;
import solutions.fairdata.fdp.index.entity.events.Event;
import solutions.fairdata.fdp.index.entity.webhooks.Webhook;
import solutions.fairdata.fdp.index.entity.webhooks.WebhookEvent;
import solutions.fairdata.fdp.index.exceptions.NotFoundException;
import solutions.fairdata.fdp.index.utils.WebhookUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;


@Service
public class WebhookService {
    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    WebhookRepository webhookRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    private EventsConfig eventsConfig;

    private static final String SECRET_PLACEHOLDER = "*** HIDDEN ***";

    public void processWebhookTrigger(Event event) {
        event.execute();
        eventRepository.save(event);
        WebhookPayloadDTO webhookPayload = WebhookUtils.preparePayload(event);
        try {
            String payloadWithSecret = objectMapper.writeValueAsString(webhookPayload);
            String signature = WebhookUtils.computeHashSignature(payloadWithSecret);
            webhookPayload.setSecret(SECRET_PLACEHOLDER);
            String payloadWithoutSecret = objectMapper.writeValueAsString(webhookPayload);
            WebhookUtils.postWebhook(event, eventsConfig.getRetrievalTimeout(), payloadWithoutSecret, signature);
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert webhook payload to string");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not compute SHA-1 signature of payload");
        }
        event.finish();
        eventRepository.save(event);
    }

    @Async
    public void triggerWebhook(Webhook webhook, WebhookEvent webhookEvent, Event triggerEvent) {
        Event event = WebhookUtils.prepareTriggerEvent(webhook, webhookEvent, triggerEvent);
        processWebhookTrigger(event);
    }

    @Async
    public void triggerWebhooks(WebhookEvent webhookEvent, Event triggerEvent) {
        logger.info("Triggered webhook event " + webhookEvent + " by event " + triggerEvent.getUuid());
        WebhookUtils.filterMatching(webhookRepository.findAll(), webhookEvent, triggerEvent).forEach(webhook -> triggerWebhook(webhook, webhookEvent, triggerEvent));
    }

    public Event handleWebhookPing(HttpServletRequest request, UUID webhookUuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Webhook> webhook = webhookRepository.findByUuid(webhookUuid);
        Event event = eventRepository.save(WebhookUtils.preparePingEvent(request, authentication, webhookUuid));
        if (webhook.isEmpty()) {
            throw new NotFoundException("There is no such webhook: " + webhookUuid);
        }
        return event;
    }

    @Async
    public void triggerWebhooks(Event triggerEvent) {
        switch (triggerEvent.getType()) {
            case AdminTrigger:
                triggerWebhooks(WebhookEvent.AdminTrigger, triggerEvent);
                break;
            case IncomingPing:
                triggerWebhooks(WebhookEvent.IncomingPing, triggerEvent);
                if (triggerEvent.getIncomingPing().getNewEntry()) {
                    triggerWebhooks(WebhookEvent.NewEntry, triggerEvent);
                }
                break;
            case MetadataRetrieval:
                switch (triggerEvent.getRelatedTo().getState()) {
                    case Valid:
                        triggerWebhooks(WebhookEvent.EntryValid, triggerEvent);
                        break;
                    case Invalid:
                        triggerWebhooks(WebhookEvent.EntryInvalid, triggerEvent);
                        break;
                    case Unreachable:
                        triggerWebhooks(WebhookEvent.EntryUnreachable, triggerEvent);
                        break;
                    default:
                        logger.warn("Invalid state of MetadataRetrieval: " + triggerEvent.getRelatedTo().getState());
                }
                break;
            case WebhookPing:
                triggerWebhooks(WebhookEvent.WebhookPing, triggerEvent);
                break;
            default:
                logger.warn("Invalid event type for webhook trigger: " + triggerEvent.getType());
        }
    }
}

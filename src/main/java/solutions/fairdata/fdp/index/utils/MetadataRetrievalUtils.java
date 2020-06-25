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
package solutions.fairdata.fdp.index.utils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.springframework.http.HttpHeaders;
import solutions.fairdata.fdp.index.entity.RepositoryMetadata;
import solutions.fairdata.fdp.index.entity.events.Event;
import solutions.fairdata.fdp.index.entity.events.EventType;
import solutions.fairdata.fdp.index.entity.events.MetadataRetrieval;
import solutions.fairdata.fdp.index.entity.http.Exchange;
import solutions.fairdata.fdp.index.entity.http.ExchangeDirection;
import solutions.fairdata.fdp.index.entity.http.ExchangeState;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;


public class MetadataRetrievalUtils {

    private static final EventType EVENT_TYPE = EventType.MetadataRetrieval;

    private static final Integer VERSION = 1;

    private static final Duration TIMEOUT = Duration.ofMinutes(1); // TODO: configurable

    private static final Duration RETRIEVAL_WAIT = Duration.ofMinutes(10); // TODO: configurable

    private static final IRI REPOSITORY = SimpleValueFactory.getInstance().createIRI("http://www.re3data.org/schema/3-0#Repository");

    private static final Map<IRI, String> MAPPING = Map.of(
            DCTERMS.TITLE, "title",
            DCTERMS.DESCRIPTION, "description",
            DCTERMS.HAS_VERSION, "version"
    );

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public static boolean shouldRetrieve(Event triggerEvent) {
        if (triggerEvent.getRelatedTo() == null) {
            return false;
        }
        Instant lastRetrieval = triggerEvent.getRelatedTo().getLastRetrievalTime();
        if (lastRetrieval == null) {
            return true;
        }
        return Duration.between(lastRetrieval, Instant.now()).compareTo(RETRIEVAL_WAIT) > 0;
    }

    public static Event prepareEvent(Event triggerEvent) {
        return new Event(VERSION, triggerEvent, triggerEvent.getRelatedTo(), new MetadataRetrieval());
    }

    public static void retrieveRepositoryMetadata(Event event) {
        if (event.getType() != EVENT_TYPE) {
            throw new IllegalArgumentException("Invalid event type");
        }
        var ex = new Exchange(ExchangeDirection.OUTGOING);
        event.getMetadataRetrieval().setExchange(ex);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(event.getRelatedTo().getClientUrl()))
                    .timeout(TIMEOUT)
                    .header(HttpHeaders.ACCEPT, RDFFormat.TURTLE.getDefaultMIMEType())
                    .GET().build();
            ex.getRequest().setFromHttpRequest(request);
            ex.setState(ExchangeState.Requested);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ex.getResponse().setFromHttpResponse(response);
            ex.setState(ExchangeState.Retrieved);
        } catch (InterruptedException e) {
            ex.setState(ExchangeState.Timeout);
            ex.setError("Timeout");
        } catch (IllegalArgumentException e) {
            ex.setState(ExchangeState.Failed);
            ex.setError("Invalid URI: " + e.getMessage());
        } catch (IOException e) {
            ex.setState(ExchangeState.Failed);
            ex.setError("IO error: " + e.getMessage());
        }
    }

    public static Optional<RepositoryMetadata> parseRepositoryMetadata(String metadata) throws IOException {
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        StatementCollector collector = new StatementCollector();
        parser.setRDFHandler(collector);

        parser.parse(new StringReader(metadata), String.valueOf(StandardCharsets.UTF_8));
        ArrayList<Statement> statements = new ArrayList<>(collector.getStatements());

        return findRepository(statements).map(repository -> extractRepositoryMetadata(statements, repository));
    }

    private static RepositoryMetadata extractRepositoryMetadata(ArrayList<Statement> statements, Resource repository) {
        var repositoryMetadata = new RepositoryMetadata();
        repositoryMetadata.setMetadataVersion(VERSION);
        repositoryMetadata.setRepositoryUri(repository.toString());

        for (Statement st: statements) {
            if (st.getSubject().equals(repository)) {
                if (MAPPING.containsKey(st.getPredicate())) {
                    repositoryMetadata.getMetadata().put(MAPPING.get(st.getPredicate()), st.getObject().stringValue());
                }
            }
        }

        return repositoryMetadata;
    }

    private static Optional<Resource> findRepository(ArrayList<Statement> statements) {
        for (Statement st: statements) {
            if (st.getPredicate().equals(RDF.TYPE) && st.getObject().equals(REPOSITORY)) {
                return Optional.of(st.getSubject());
            }
        }
        return Optional.empty();
    }
}

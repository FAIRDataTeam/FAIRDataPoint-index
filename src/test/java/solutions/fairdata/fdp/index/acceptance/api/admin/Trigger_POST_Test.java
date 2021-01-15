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
package solutions.fairdata.fdp.index.acceptance.api.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import solutions.fairdata.fdp.index.WebIntegrationTest;
import solutions.fairdata.fdp.index.database.repository.EventRepository;
import solutions.fairdata.fdp.index.database.repository.IndexEntryRepository;
import solutions.fairdata.fdp.index.database.repository.TokenRepository;
import solutions.fairdata.fdp.index.entity.IndexEntry;
import solutions.fairdata.fdp.index.entity.Token;
import solutions.fairdata.fdp.index.entity.events.Event;
import solutions.fairdata.fdp.index.entity.events.EventType;
import solutions.fairdata.fdp.index.fixtures.IndexEntryFixtures;
import solutions.fairdata.fdp.index.fixtures.TokenFixtures;

import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("POST /admin/trigger")
public class Trigger_POST_Test extends WebIntegrationTest {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private IndexEntryRepository indexEntryRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    private final ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<>() {};

    private URI url() {
        return URI.create("/admin/trigger");
    }

    private URI url(String clientUrl) {
        return URI.create("/admin/trigger?clientUrl=" + clientUrl);
    }

    @Test
    @DisplayName("HTTP 403: no token")
    public void res403_noToken() {
        // GIVEN (prepare data)
        String clientUrl = "http://example.com";
        mongoTemplate.getDb().drop();

        // AND (prepare request)
        RequestEntity<Void> request = RequestEntity
                .post(url(clientUrl))
                .build();

        // WHEN
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.FORBIDDEN)));
    }

    @Test
    @DisplayName("HTTP 403: incorrect token")
    public void res403_incorrectToken() {
        // GIVEN (prepare data)
        String clientUrl = "http://example.com";
        Token token = TokenFixtures.adminToken();
        mongoTemplate.getDb().drop();
        tokenRepository.save(token);

        // AND (prepare request)
        RequestEntity<Void> request = RequestEntity
                .post(url(clientUrl))
                .header(HttpHeaders.AUTHORIZATION, "Bearer myIncorrectToken321")
                .build();

        // WHEN
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.FORBIDDEN)));
    }

    @Test
    @DisplayName("HTTP 403: non-admin token")
    public void res403_nonAdminToken() {
        // GIVEN (prepare data)
        String clientUrl = "http://example.com";
        Token token = TokenFixtures.noRoleToken();
        mongoTemplate.getDb().drop();
        tokenRepository.save(token);

        // AND (prepare request)
        RequestEntity<Void> request = RequestEntity
                .post(url(clientUrl))
                .header(HttpHeaders.AUTHORIZATION, "Bearer myIncorrectToken321")
                .build();

        // WHEN
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.FORBIDDEN)));
    }

    @Test
    @DisplayName("HTTP 204: trigger one")
    public void res204_triggerOne() {
        // GIVEN (prepare data)
        IndexEntry entry = IndexEntryFixtures.entryExample();
        Token token = TokenFixtures.adminToken();
        mongoTemplate.getDb().drop();
        indexEntryRepository.save(entry);
        tokenRepository.save(token);

        // AND (prepare request)
        RequestEntity<Void> request = RequestEntity
                .post(url(entry.getClientUrl()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getToken())
                .build();

        // WHEN
        ResponseEntity<Void> result = client.exchange(request, responseType);
        List<Event> events = eventRepository.getAllByType(EventType.AdminTrigger);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
        assertThat("One AdminTrigger event is created", events.size(), is(equalTo(1)));
        assertThat("Records correct token name", events.get(0).getAdminTrigger().getTokenName(), is(equalTo(token.getName())));
        assertThat("Records correct client URL", events.get(0).getAdminTrigger().getClientUrl(), is(equalTo(entry.getClientUrl())));
    }

    @Test
    @DisplayName("HTTP 204: trigger all")
    public void res204_triggerAll() {
        // GIVEN (prepare data)
        Token token = TokenFixtures.adminToken();
        mongoTemplate.getDb().drop();
        tokenRepository.save(token);

        // AND (prepare request)
        RequestEntity<Void> request = RequestEntity
                .post(url())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getToken())
                .build();

        // WHEN
        ResponseEntity<Void> result = client.exchange(request, responseType);
        List<Event> events = eventRepository.getAllByType(EventType.AdminTrigger);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
        assertThat("One AdminTrigger event is created", events.size(), is(equalTo(1)));
        assertThat("Records correct token name", events.get(0).getAdminTrigger().getTokenName(), is(equalTo(token.getName())));
        assertThat("Records correct client URL as null", events.get(0).getAdminTrigger().getClientUrl(), is(equalTo(null)));
    }

    @Test
    @DisplayName("HTTP 404: trigger non-existing")
    public void res404_triggerOne() {
        // GIVEN (prepare data)
        IndexEntry entry = IndexEntryFixtures.entryExample();
        Token token = TokenFixtures.adminToken();
        mongoTemplate.getDb().drop();
        tokenRepository.save(token);

        // AND (prepare request)
        RequestEntity<Void> request = RequestEntity
                .post(url(entry.getClientUrl()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getToken())
                .build();

        // WHEN
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
    }
}

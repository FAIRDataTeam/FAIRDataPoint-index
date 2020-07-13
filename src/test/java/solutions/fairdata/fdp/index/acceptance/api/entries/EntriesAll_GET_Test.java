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
package solutions.fairdata.fdp.index.acceptance.api.entries;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import solutions.fairdata.fdp.index.WebIntegrationTest;
import solutions.fairdata.fdp.index.api.dto.IndexEntryDTO;
import solutions.fairdata.fdp.index.database.repository.IndexEntryRepository;
import solutions.fairdata.fdp.index.entity.IndexEntry;
import solutions.fairdata.fdp.index.fixtures.IndexEntryFixtures;

import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@DisplayName("GET /entries/all")
public class EntriesAll_GET_Test extends WebIntegrationTest {

    @Autowired
    private IndexEntryRepository indexEntryRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    private final ParameterizedTypeReference<List<IndexEntryDTO>> responseType = new ParameterizedTypeReference<>() {};

    private URI url() {
        return URI.create("/entries/all");
    }

    @Test
    @DisplayName("HTTP 200: list empty")
    public void res200_listEmpty() {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(url())
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // WHEN
        ResponseEntity<List<IndexEntryDTO>> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("Response body is not null", result.getBody(), is(notNullValue()));
        assertThat("There are no entries in the response", result.getBody().size(), is(equalTo(0)));
    }

    @Test
    @DisplayName("HTTP 200: list few")
    public void res200_listFew() {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();
        List<IndexEntry> entries = IndexEntryFixtures.entriesFew();
        indexEntryRepository.saveAll(entries);

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(url())
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // WHEN
        ResponseEntity<List<IndexEntryDTO>> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("Response body is not null", result.getBody(), is(notNullValue()));
        assertThat("Correct number of entries is in the response", result.getBody().size(), is(equalTo(entries.size())));
        for (int i = 0; i < entries.size(); i++) {
            assertThat("Entry matches: " + entries.get(i).getClientUrl(), result.getBody().get(i).getClientUrl(), is(equalTo(entries.get(i).getClientUrl())));
        }
    }

    @Test
    @DisplayName("HTTP 200: list many")
    public void res200_listMany() {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();
        List<IndexEntry> entries = IndexEntryFixtures.entriesN(300);
        indexEntryRepository.saveAll(entries);

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(url())
                .accept(MediaType.APPLICATION_JSON)
                .build();

        // WHEN
        ResponseEntity<List<IndexEntryDTO>> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("Response body is not null", result.getBody(), is(notNullValue()));
        assertThat("Correct number of entries is in the response", result.getBody().size(), is(equalTo(entries.size())));
        for (int i = 0; i < entries.size(); i++) {
            assertThat("Entry matches: " + entries.get(i).getClientUrl(), result.getBody().get(i).getClientUrl(), is(equalTo(entries.get(i).getClientUrl())));
        }
    }
}

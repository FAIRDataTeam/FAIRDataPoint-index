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
import solutions.fairdata.fdp.index.api.dto.EntryDTO;
import solutions.fairdata.fdp.index.domain.IndexEntry;
import solutions.fairdata.fdp.index.fixtures.IndexEntryFixtures;
import solutions.fairdata.fdp.index.storage.EntryRepository;

import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class EntriesAll_GET extends WebIntegrationTest {

    private URI url() {
        return URI.create("/entries/all");
    }

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

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
        ParameterizedTypeReference<List<EntryDTO>> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<List<EntryDTO>> result = client.exchange(request, responseType);

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
        entryRepository.saveAll(entries);

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(url())
                .accept(MediaType.APPLICATION_JSON)
                .build();
        ParameterizedTypeReference<List<EntryDTO>> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<List<EntryDTO>> result = client.exchange(request, responseType);

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
        entryRepository.saveAll(entries);

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(url())
                .accept(MediaType.APPLICATION_JSON)
                .build();
        ParameterizedTypeReference<List<EntryDTO>> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<List<EntryDTO>> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("Response body is not null", result.getBody(), is(notNullValue()));
        assertThat("Correct number of entries is in the response", result.getBody().size(), is(equalTo(entries.size())));
        for (int i = 0; i < entries.size(); i++) {
            assertThat("Entry matches: " + entries.get(i).getClientUrl(), result.getBody().get(i).getClientUrl(), is(equalTo(entries.get(i).getClientUrl())));
        }
    }
}

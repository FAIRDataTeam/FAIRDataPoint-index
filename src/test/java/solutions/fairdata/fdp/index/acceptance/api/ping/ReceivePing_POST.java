package solutions.fairdata.fdp.index.acceptance.api.ping;

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
import solutions.fairdata.fdp.index.api.dto.PingDTO;
import solutions.fairdata.fdp.index.domain.IndexEntry;
import solutions.fairdata.fdp.index.fixtures.IndexEntryFixtures;
import solutions.fairdata.fdp.index.storage.EntryRepository;

import java.net.URI;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ReceivePing_POST extends WebIntegrationTest {

    private URI url() {
        return URI.create("/");
    }

    private PingDTO reqDTO(String clientUrl) {
        PingDTO dto = new PingDTO();
        dto.setClientUrl(clientUrl);
        return dto;
    }

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("HTTP 204: new entry")
    public void res204_newEnty() {
        // GIVEN (prepare data)
        String clientUrl = "http://example.com";
        mongoTemplate.getDb().drop();
        PingDTO reqDto = reqDTO(clientUrl);

        // AND (prepare request)
        RequestEntity<PingDTO> request = RequestEntity
                .post(url())
                .accept(MediaType.APPLICATION_JSON)
                .body(reqDto);
        ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        assertThat("Entry does not exist before the ping", entryRepository.findByClientUrl(clientUrl).isPresent(), is(Boolean.FALSE));
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
        assertThat("Entry exists after the ping", entryRepository.findByClientUrl(clientUrl).isPresent(), is(Boolean.TRUE));
    }

    @Test
    @DisplayName("HTTP 204: existing entry")
    public void res204_existingEnty() {
        // GIVEN (prepare data)
        IndexEntry indexEntry = IndexEntryFixtures.entryExample();
        String clientUrl = indexEntry.getClientUrl();
        mongoTemplate.getDb().drop();
        PingDTO reqDto = reqDTO(clientUrl);

        // AND (prepare request)
        RequestEntity<PingDTO> request = RequestEntity
                .post(url())
                .accept(MediaType.APPLICATION_JSON)
                .body(reqDto);
        ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        entryRepository.save(indexEntry);
        assertThat("Entry exists before the ping", entryRepository.findByClientUrl(clientUrl).isPresent(), is(Boolean.TRUE));
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
        assertThat("Entry exists after the ping", entryRepository.findByClientUrl(clientUrl).isPresent(), is(Boolean.TRUE));
    }

    @Test
    @DisplayName("HTTP 400: null client url")
    public void res400_nullClientUrl() {
        // GIVEN (prepare data)
        PingDTO reqDto = reqDTO(null);

        // AND (prepare request)
        RequestEntity<PingDTO> request = RequestEntity
                .post(url())
                .accept(MediaType.APPLICATION_JSON)
                .body(reqDto);
        ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
    }

    @Test
    @DisplayName("HTTP 400: non-URL client url")
    public void res400_nonUrlClientUrl() {
        // GIVEN (prepare data)
        PingDTO reqDto = reqDTO("testing");

        // AND (prepare request)
        RequestEntity<PingDTO> request = RequestEntity
                .post(url())
                .accept(MediaType.APPLICATION_JSON)
                .body(reqDto);
        ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
    }

    @Test
    @DisplayName("HTTP 400: different body")
    public void res400_differentBody() {
        // GIVEN (prepare data)
        HashMap<String, String> dummyData = new HashMap<>();
        dummyData.put("content", "http://test");

        // AND (prepare request)
        RequestEntity<HashMap<String, String>> request = RequestEntity
                .post(url())
                .accept(MediaType.APPLICATION_JSON)
                .body(dummyData);
        ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
    }
}
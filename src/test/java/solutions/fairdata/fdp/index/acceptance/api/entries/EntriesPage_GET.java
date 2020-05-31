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
import org.springframework.web.util.UriComponentsBuilder;
import solutions.fairdata.fdp.index.WebIntegrationTest;
import solutions.fairdata.fdp.index.api.dto.EntryDTO;
import solutions.fairdata.fdp.index.domain.IndexEntry;
import solutions.fairdata.fdp.index.fixtures.IndexEntryFixtures;
import solutions.fairdata.fdp.index.storage.EntryRepository;
import solutions.fairdata.fdp.index.utils.CustomPageImpl;

import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;


public class EntriesPage_GET extends WebIntegrationTest {

    @Autowired
    private EntryRepository entryRepository;

    private URI url() {
        return URI.create("/entries");
    }

    private URI urlWithPage(int page) {
        return UriComponentsBuilder.fromUri(url())
                .queryParam("page", page)
                .build().toUri();
    }

    private URI urlWithPageSize(int page, int size) {
        return UriComponentsBuilder.fromUri(url())
                .queryParam("page", page)
                .queryParam("size", size)
                .build().toUri();
    }

    @Test
    @DisplayName("HTTP 200: page empty")
    public void res200_pageEmpty() {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(url())
                .accept(MediaType.APPLICATION_JSON)
                .build();
        ParameterizedTypeReference<CustomPageImpl<EntryDTO>> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<CustomPageImpl<EntryDTO>> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("Response body is not null", result.getBody(), is(notNullValue()));
        assertThat("Current page is the first page", result.getBody().isFirst(), is(Boolean.TRUE));
        assertThat("Current page is the last page", result.getBody().isLast(), is(Boolean.TRUE));
        assertThat("Current page is empty", result.getBody().isEmpty(), is(Boolean.TRUE));
        assertThat("Number of pages is 0", result.getBody().getTotalPages(), is(equalTo(0)));
        assertThat("Number of elements is 0", result.getBody().getTotalElements(), is(equalTo(0L)));
        assertThat("There are no entries in the response", result.getBody().getContent().size(), is(equalTo(0)));
    }

    @Test
    @DisplayName("HTTP 200: out-of-bounds page")
    public void res200_outOfBoundsPage() {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(urlWithPage(7))
                .accept(MediaType.APPLICATION_JSON)
                .build();
        ParameterizedTypeReference<CustomPageImpl<EntryDTO>> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<CustomPageImpl<EntryDTO>> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("Response body is not null", result.getBody(), is(notNullValue()));
        assertThat("Current page is not the first page", result.getBody().isFirst(), is(Boolean.FALSE));
        assertThat("Current page is the last page", result.getBody().isLast(), is(Boolean.TRUE));
        assertThat("Current page is empty", result.getBody().isEmpty(), is(Boolean.TRUE));
        assertThat("Number of pages is 0", result.getBody().getTotalPages(), is(equalTo(0)));
        assertThat("Number of elements is 0", result.getBody().getTotalElements(), is(equalTo(0L)));
        assertThat("There are no entries in the response", result.getBody().getContent().size(), is(equalTo(0)));
    }

    @Test
    @DisplayName("HTTP 200: page few")
    public void res200_pageFew() {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();
        List<IndexEntry> entries = IndexEntryFixtures.entriesFew();
        entryRepository.saveAll(entries);

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(url())
                .accept(MediaType.APPLICATION_JSON)
                .build();
        ParameterizedTypeReference<CustomPageImpl<EntryDTO>> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<CustomPageImpl<EntryDTO>> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("Response body is not null", result.getBody(), is(notNullValue()));
        assertThat("Current page is the first page", result.getBody().isFirst(), is(Boolean.TRUE));
        assertThat("Current page is the last page", result.getBody().isLast(), is(Boolean.TRUE));
        assertThat("Current page is not empty", result.getBody().isEmpty(), is(Boolean.FALSE));
        assertThat("Number of pages is 1", result.getBody().getTotalPages(), is(equalTo(1)));
        assertThat("Number of elements is correct", result.getBody().getTotalElements(), is(equalTo(Integer.toUnsignedLong(entries.size()))));
        assertThat("There is correct number of entries in the response", result.getBody().getContent().size(), is(equalTo(entries.size())));
        for (int i = 0; i < entries.size(); i++) {
            assertThat("Entry matches: " + entries.get(i).getClientUrl(), result.getBody().getContent().get(i).getClientUrl(), is(equalTo(entries.get(i).getClientUrl())));
        }
    }

    @Test
    @DisplayName("HTTP 200: page many (middle)")
    public void res200_pageManyMiddle() {
        // GIVEN (prepare data)
        Integer items = 300;
        int size = 30;
        int page = 3;
        mongoTemplate.getDb().drop();
        List<IndexEntry> entries = IndexEntryFixtures.entriesN(items);
        entryRepository.saveAll(entries);

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(urlWithPageSize(page, size))
                .accept(MediaType.APPLICATION_JSON)
                .build();
        ParameterizedTypeReference<CustomPageImpl<EntryDTO>> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<CustomPageImpl<EntryDTO>> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("Response body is not null", result.getBody(), is(notNullValue()));
        assertThat("Current page is the first page", result.getBody().isFirst(), is(Boolean.FALSE));
        assertThat("Current page is the last page", result.getBody().isLast(), is(Boolean.FALSE));
        assertThat("Current page is not empty", result.getBody().isEmpty(), is(Boolean.FALSE));
        assertThat("Number of pages is correct", result.getBody().getTotalPages(), is(equalTo(10)));
        assertThat("Number of elements is correct", result.getBody().getTotalElements(), is(equalTo(items.longValue())));
        assertThat("There is correct number of entries in the response", result.getBody().getContent().size(), is(equalTo(size)));
    }

    @Test
    @DisplayName("HTTP 200: page many (last)")
    public void res200_pageManyLast() {
        // GIVEN (prepare data)
        Integer items = 666;
        int size = 300;
        int lastSize = 66;
        int page = 3;
        mongoTemplate.getDb().drop();
        List<IndexEntry> entries = IndexEntryFixtures.entriesN(items);
        entryRepository.saveAll(entries);

        // AND (prepare request)
        RequestEntity<?> request = RequestEntity
                .get(urlWithPageSize(page, size))
                .accept(MediaType.APPLICATION_JSON)
                .build();
        ParameterizedTypeReference<CustomPageImpl<EntryDTO>> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN
        ResponseEntity<CustomPageImpl<EntryDTO>> result = client.exchange(request, responseType);

        // THEN
        assertThat("Correct response code is received", result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("Response body is not null", result.getBody(), is(notNullValue()));
        assertThat("Current page is the first page", result.getBody().isFirst(), is(Boolean.FALSE));
        assertThat("Current page is the last page", result.getBody().isLast(), is(Boolean.TRUE));
        assertThat("Current page is not empty", result.getBody().isEmpty(), is(Boolean.FALSE));
        assertThat("Number of pages is correct", result.getBody().getTotalPages(), is(equalTo(3)));
        assertThat("Number of elements is correct", result.getBody().getTotalElements(), is(equalTo(items.longValue())));
        assertThat("There is correct number of entries in the response", result.getBody().getContent().size(), is(equalTo(lastSize)));
    }
}

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
package solutions.fairdata.fdp.index.acceptance.web.home;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.UriComponentsBuilder;
import solutions.fairdata.fdp.index.WebIntegrationTest;
import solutions.fairdata.fdp.index.database.repository.IndexEntryRepository;
import solutions.fairdata.fdp.index.entity.IndexEntry;
import solutions.fairdata.fdp.index.entity.config.EventsConfig;
import solutions.fairdata.fdp.index.fixtures.IndexEntryFixtures;

import java.net.URI;
import java.time.Instant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@DisplayName("Browse /entry")
public class Entry_GET_Test extends WebIntegrationTest {

    @Autowired
    private IndexEntryRepository indexEntryRepository;

    @Autowired
    private EventsConfig eventsConfig;

    private URI url(String clientUrl) {
        return UriComponentsBuilder.fromUriString("/entry")
                .queryParam("clientUrl", clientUrl)
                .build().toUri();
    }

    @Test
    @DisplayName("HTTP 200: unknown entry")
    public void res200_unknownEntry() throws Exception {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();

        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders
                .get(url("http://example.com"))
                .accept(MediaType.TEXT_HTML);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result
                .andExpect(status().isOk())
                .andExpect(view().name("entry"))
                .andExpect(xpath("//div[@class='entry-label']/span").string("Not found"))
                .andExpect(xpath("//h2[@class='entry-title']/a").string("http://example.com"))
                .andExpect(xpath("//h2[@class='entry-title']/a/@href").string("http://example.com"))
                .andExpect(xpath("//div[@id='entryEmpty']").exists())
                .andExpect(xpath("//div[@id='entryFound']").doesNotExist());
    }

    @Test
    @DisplayName("HTTP 200: reachable entry")
    public void res200_reachableEntry() throws Exception {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();
        IndexEntry indexEntry = IndexEntryFixtures.activeEntry("http://example.com");
        indexEntryRepository.save(indexEntry);

        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders
                .get(url(indexEntry.getClientUrl()))
                .accept(MediaType.TEXT_HTML);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result
                .andExpect(status().isOk())
                .andExpect(view().name("entry"))
                .andExpect(xpath("//div[@class='entry-label']/a").string("Active"))
                .andExpect(xpath("//div[@class='entry-label']/a/@href").string(indexEntry.getClientUrl()))
                .andExpect(xpath("//h2[@class='entry-title']/a").string(indexEntry.getClientUrl()))
                .andExpect(xpath("//h2[@class='entry-title']/a/@href").string(indexEntry.getClientUrl()))
                .andExpect(xpath("//div[@id='entryEmpty']").doesNotExist())
                .andExpect(xpath("//div[@id='entryFound']").exists())
                .andExpect(xpath("//*[@id='repository-uri']/a").string(indexEntry.getCurrentMetadata().getRepositoryUri()))
                .andExpect(xpath("//*[@id='repository-uri']/a/@href").string(indexEntry.getCurrentMetadata().getRepositoryUri()))
                .andExpect(xpath("//*[@id='metadata-title']").string(indexEntry.getCurrentMetadata().getMetadata().get("title")))
                .andExpect(xpath("//*[@id='metadata-version']").string(indexEntry.getCurrentMetadata().getMetadata().get("version")))
                .andExpect(xpath("//*[@id='metadata-description']").string(indexEntry.getCurrentMetadata().getMetadata().get("description")));
    }

    @Test
    @DisplayName("HTTP 200: unreachable entry")
    public void res200_unreachableEntry() throws Exception {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();
        IndexEntry indexEntry = IndexEntryFixtures.activeEntry("http://example.com");
        Instant oldDate = Instant.now().minus(eventsConfig.getPingValidDuration());
        indexEntry.setRegistrationTime(oldDate);
        indexEntry.setModificationTime(oldDate);
        indexEntry.setLastRetrievalTime(oldDate);
        indexEntryRepository.save(indexEntry);

        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders
                .get(url(indexEntry.getClientUrl()))
                .accept(MediaType.TEXT_HTML);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result
                .andExpect(status().isOk())
                .andExpect(view().name("entry"))
                .andExpect(xpath("//div[@class='entry-label']/span").string("Inactive"))
                .andExpect(xpath("//h2[@class='entry-title']/a").string(indexEntry.getClientUrl()))
                .andExpect(xpath("//h2[@class='entry-title']/a/@href").string(indexEntry.getClientUrl()))
                .andExpect(xpath("//div[@id='entryEmpty']").doesNotExist())
                .andExpect(xpath("//div[@id='entryFound']").exists())
                .andExpect(xpath("//*[@id='repository-uri']/a").string(indexEntry.getCurrentMetadata().getRepositoryUri()))
                .andExpect(xpath("//*[@id='repository-uri']/a/@href").string(indexEntry.getCurrentMetadata().getRepositoryUri()))
                .andExpect(xpath("//*[@id='metadata-title']").string(indexEntry.getCurrentMetadata().getMetadata().get("title")))
                .andExpect(xpath("//*[@id='metadata-version']").string(indexEntry.getCurrentMetadata().getMetadata().get("version")))
                .andExpect(xpath("//*[@id='metadata-description']").string(indexEntry.getCurrentMetadata().getMetadata().get("description")));
    }

    @Test
    @DisplayName("HTTP 200: invalid entry")
    public void res200_invalidEntry() throws Exception {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();
        IndexEntry indexEntry = IndexEntryFixtures.entryExample();
        indexEntryRepository.save(indexEntry);

        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders
                .get(url(indexEntry.getClientUrl()))
                .accept(MediaType.TEXT_HTML);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result
                .andExpect(status().isOk())
                .andExpect(view().name("entry"))
                .andExpect(xpath("//div[@class='entry-label']/span").string("Invalid"))
                .andExpect(xpath("//h2[@class='entry-title']/a").string(indexEntry.getClientUrl()))
                .andExpect(xpath("//h2[@class='entry-title']/a/@href").string(indexEntry.getClientUrl()))
                .andExpect(xpath("//div[@id='entryEmpty']").doesNotExist())
                .andExpect(xpath("//div[@id='entryFound']").exists())
                .andExpect(xpath("//*[@id='repository-uri']").doesNotExist())
                .andExpect(xpath("//*[@id='metadata-title']").doesNotExist());
    }
}

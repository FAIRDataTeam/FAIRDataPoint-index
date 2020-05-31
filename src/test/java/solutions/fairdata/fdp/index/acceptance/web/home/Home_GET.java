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
import solutions.fairdata.fdp.index.domain.IndexEntry;
import solutions.fairdata.fdp.index.fixtures.IndexEntryFixtures;
import solutions.fairdata.fdp.index.storage.EntryRepository;

import java.net.URI;
import java.util.List;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class Home_GET extends WebIntegrationTest {

    private URI url() {
        return URI.create("/");
    }

    private URI urlWithPageSize(int page, int size) {
        return UriComponentsBuilder.fromUri(url())
                .queryParam("page", page)
                .queryParam("size", size)
                .build().toUri();
    }

    @Autowired
    private EntryRepository entryRepository;

    @Test
    @DisplayName("HTTP 200: empty table")
    public void res200_emptyTable() throws Exception {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();

        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders
                .get(url())
                .accept(MediaType.TEXT_HTML);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(xpath("//table[@id='entries']/thead/tr").exists())
                .andExpect(xpath("//table[@id='entries']/tbody/tr").doesNotExist())
                .andExpect(xpath("//span[@id='totalEntries']").string("0"))
                .andExpect(xpath("//span[@id='currentPage']").string("1"))
                .andExpect(xpath("//span[@id='totalPages']").string("1"))
                .andExpect(xpath("//*[@id='firstPage']/a").doesNotExist())
                .andExpect(xpath("//*[@id='previousPage']/a").doesNotExist())
                .andExpect(xpath("//*[@id='nextPage']/a").doesNotExist())
                .andExpect(xpath("//*[@id='lastPage']/a").doesNotExist());
    }

    @Test
    @DisplayName("HTTP 200: single page")
    public void res200_singlePage() throws Exception {
        // GIVEN (prepare data)
        mongoTemplate.getDb().drop();
        List<IndexEntry> entries = IndexEntryFixtures.entriesFew();
        entryRepository.saveAll(entries);

        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders
                .get(url())
                .accept(MediaType.TEXT_HTML);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(xpath("//table[@id='entries']/thead/tr").exists())
                .andExpect(xpath("//table[@id='entries']/tbody/tr").nodeCount(entries.size()))
                .andExpect(xpath("//table[@id='entries']/tbody/tr[1]/td[@class='endpoint']/a").string(entries.get(0).getClientUrl()))
                .andExpect(xpath("//table[@id='entries']/tbody/tr[1]/td[@class='endpoint']/a/@href").string(entries.get(0).getClientUrl()))
                .andExpect(xpath("//span[@id='totalEntries']").string(String.valueOf(entries.size())))
                .andExpect(xpath("//span[@id='currentPage']").string("1"))
                .andExpect(xpath("//span[@id='totalPages']").string("1"))
                .andExpect(xpath("//*[@id='firstPage']/a").doesNotExist())
                .andExpect(xpath("//*[@id='previousPage']/a").doesNotExist())
                .andExpect(xpath("//*[@id='nextPage']/a").doesNotExist())
                .andExpect(xpath("//*[@id='lastPage']/a").doesNotExist());
    }

    @Test
    @DisplayName("HTTP 200: first page of many")
    public void res200_firstPageOfMany() throws Exception {
        // GIVEN (prepare data)
        int items = 333;
        int size = 50;
        mongoTemplate.getDb().drop();
        List<IndexEntry> entries = IndexEntryFixtures.entriesN(items);
        entryRepository.saveAll(entries);

        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders
                .get(urlWithPageSize(1, size))
                .accept(MediaType.TEXT_HTML);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(xpath("//table[@id='entries']/thead/tr").exists())
                .andExpect(xpath("//table[@id='entries']/tbody/tr").nodeCount(size))
                .andExpect(xpath("//table[@id='entries']/tbody/tr[1]/td[@class='endpoint']/a").string(entries.get(0).getClientUrl()))
                .andExpect(xpath("//table[@id='entries']/tbody/tr[1]/td[@class='endpoint']/a/@href").string(entries.get(0).getClientUrl()))
                .andExpect(xpath("//span[@id='totalEntries']").string(String.valueOf(items)))
                .andExpect(xpath("//span[@id='currentPage']").string("1"))
                .andExpect(xpath("//span[@id='totalPages']").string("7"))
                .andExpect(xpath("//*[@id='firstPage']/a").doesNotExist())
                .andExpect(xpath("//*[@id='previousPage']/a").doesNotExist())
                .andExpect(xpath("//*[@id='nextPage']/a").exists())
                .andExpect(xpath("//*[@id='lastPage']/a").exists());
    }

    @Test
    @DisplayName("HTTP 200: last page of many")
    public void res200_lastPageOfMany() throws Exception {
        // GIVEN (prepare data)
        int items = 333;
        int size = 50;
        int page = 7;
        mongoTemplate.getDb().drop();
        List<IndexEntry> entries = IndexEntryFixtures.entriesN(items);
        entryRepository.saveAll(entries);

        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders
                .get(urlWithPageSize(page, size))
                .accept(MediaType.TEXT_HTML);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(xpath("//table[@id='entries']/thead/tr").exists())
                .andExpect(xpath("//table[@id='entries']/tbody/tr").nodeCount(33))
                .andExpect(xpath("//table[@id='entries']/tbody/tr[1]/td[@class='endpoint']/a").string(entries.get((page-1)*size).getClientUrl()))
                .andExpect(xpath("//table[@id='entries']/tbody/tr[1]/td[@class='endpoint']/a/@href").string(entries.get((page-1)*size).getClientUrl()))
                .andExpect(xpath("//span[@id='totalEntries']").string(String.valueOf(items)))
                .andExpect(xpath("//span[@id='currentPage']").string(String.valueOf(page)))
                .andExpect(xpath("//span[@id='totalPages']").string(String.valueOf(page)))
                .andExpect(xpath("//*[@id='firstPage']/a").exists())
                .andExpect(xpath("//*[@id='previousPage']/a").exists())
                .andExpect(xpath("//*[@id='nextPage']/a").doesNotExist())
                .andExpect(xpath("//*[@id='lastPage']/a").doesNotExist());
    }

    @Test
    @DisplayName("HTTP 200: middle page of many")
    public void res200_middlePageOfMany() throws Exception {
        // GIVEN (prepare data)
        int items = 333;
        int size = 50;
        int page = 4;
        mongoTemplate.getDb().drop();
        List<IndexEntry> entries = IndexEntryFixtures.entriesN(items);
        entryRepository.saveAll(entries);

        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders
                .get(urlWithPageSize(page, size))
                .accept(MediaType.TEXT_HTML);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(xpath("//table[@id='entries']/thead/tr").exists())
                .andExpect(xpath("//table[@id='entries']/tbody/tr").nodeCount(size))
                .andExpect(xpath("//table[@id='entries']/tbody/tr[1]/td[@class='endpoint']/a").string(entries.get((page-1)*size).getClientUrl()))
                .andExpect(xpath("//table[@id='entries']/tbody/tr[1]/td[@class='endpoint']/a/@href").string(entries.get((page-1)*size).getClientUrl()))
                .andExpect(xpath("//span[@id='totalEntries']").string(String.valueOf(items)))
                .andExpect(xpath("//span[@id='currentPage']").string(String.valueOf(page)))
                .andExpect(xpath("//span[@id='totalPages']").string(String.valueOf(7)))
                .andExpect(xpath("//*[@id='firstPage']/a").exists())
                .andExpect(xpath("//*[@id='previousPage']/a").exists())
                .andExpect(xpath("//*[@id='nextPage']/a").exists())
                .andExpect(xpath("//*[@id='lastPage']/a").exists());
    }
}

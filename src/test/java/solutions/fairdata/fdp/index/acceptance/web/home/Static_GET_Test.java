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
import solutions.fairdata.fdp.index.fixtures.IndexEntryFixtures;

import java.net.URI;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Retrieve static files")
public class Static_GET_Test extends WebIntegrationTest {

    private void simpleGetTest(String url) throws Exception {
        // AND (prepare request)
        RequestBuilder request = MockMvcRequestBuilders.get(url);

        // WHEN
        ResultActions result = mvc.perform(request);

        // THEN
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("HTTP 200: site.css")
    public void res200_siteCss() throws Exception {
        simpleGetTest("/css/site.css");
    }

    @Test
    @DisplayName("HTTP 200: favicon")
    public void res200_favicon() throws Exception {
        simpleGetTest("/img/favicon.png");
    }

    @Test
    @DisplayName("HTTP 200: jQuery (JS)")
    public void res200_jqueryJs() throws Exception {
        simpleGetTest("/js/jquery/jquery.slim.min.js");
    }

    @Test
    @DisplayName("HTTP 200: Bootstrap (JS)")
    public void res200_boostrapJs() throws Exception {
        simpleGetTest("/js/bootstrap/bootstrap.min.js");
    }
}

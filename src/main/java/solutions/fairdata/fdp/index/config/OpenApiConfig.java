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
package solutions.fairdata.fdp.index.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class OpenApiConfig {

    @Autowired
    BuildProperties buildProperties;

    @Bean
    public OpenAPI customOpenAPI(@Value("${fdp-index.api.url:#{null}}") String serverUrl,
                                 @Value("${fdp-index.api.title:#{null}}") String title,
                                 @Value("${fdp-index.api.description:#{null}}") String description,
                                 @Value("${fdp-index.api.contactUrl:#{null}}") String contactUrl,
                                 @Value("${fdp-index.api.contactName:#{null}}") String contactName) {
        String version = buildProperties.getVersion();
        OpenAPI openAPI = new OpenAPI()
                .components(new Components())
                .info(new Info().title(title).description(description).version(version));
        if (contactUrl != null) {
            openAPI.getInfo().contact(new Contact().url(contactUrl).name(contactName));
        }
        if (serverUrl != null) {
            openAPI.servers(Collections.singletonList(new Server().url(serverUrl)));
        }
        return openAPI;
    }
}

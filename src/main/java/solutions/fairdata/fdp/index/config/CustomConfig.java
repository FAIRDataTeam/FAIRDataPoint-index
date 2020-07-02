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

import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import solutions.fairdata.fdp.index.entity.config.EventsConfig;

import java.time.Duration;

@Configuration
public class CustomConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public EventsConfig eventsConfig(
            @Value("${fdp-index.events.retrieval.rateLimitWait:PT10M}") String cfgRetrievalRateLimitWait,
            @Value("${fdp-index.events.retrieval.timeout:PT1M}") String cfgRetrievalTimeout,
            @Value("${fdp-index.events.ping.validDuration:P7D}") String cfgPingValidDuration
    ) {
        return EventsConfig.builder()
                .retrievalRateLimitWait(Duration.parse(cfgRetrievalRateLimitWait))
                .retrievalTimeout(Duration.parse(cfgRetrievalTimeout))
                .pingValidDuration(Duration.parse(cfgPingValidDuration))
                .build();
    }
}

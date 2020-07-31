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
package solutions.fairdata.fdp.index.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import solutions.fairdata.fdp.index.api.dto.PingDTO;
import solutions.fairdata.fdp.index.entity.events.Event;
import solutions.fairdata.fdp.index.service.EventService;
import solutions.fairdata.fdp.index.service.WebhookService;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "Ping")
@RestController
@RequestMapping("/")
public class PingController {
    private static final Logger logger = LoggerFactory.getLogger(PingController.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private WebhookService webhookService;

    @Operation(
            description = "Inform about running FAIR Data Point. It is expected to send pings regularly (at least weekly). There is a rate limit set both per single IP within a period of time and per URL in message.",
            requestBody = @RequestBody(
                    description = "Ping payload with FAIR Data Point info",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(value = "{\"clientUrl\": \"https://example.com\"}")
                            },
                            schema = @Schema(
                                    type = "object",
                                    title = "Ping",
                                    implementation = PingDTO.class
                            )
                    )
            ),
            responses = {
                @ApiResponse(responseCode = "204", description = "Ping accepted (no content)"),
                @ApiResponse(responseCode = "400", description = "Invalid ping format"),
                @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receivePing(HttpEntity<String> httpEntity, HttpServletRequest request) {
        logger.info("Received ping from {}", request.getRemoteAddr());
        final Event event = eventService.acceptIncomingPing(httpEntity, request);
        logger.info("Triggering metadata retrieval for {}", event.getRelatedTo().getClientUrl());
        eventService.triggerMetadataRetrieval(event);
        webhookService.triggerWebhooks(event);
    }
}

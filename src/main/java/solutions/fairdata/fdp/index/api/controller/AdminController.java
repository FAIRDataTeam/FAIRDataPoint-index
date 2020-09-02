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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import solutions.fairdata.fdp.index.entity.events.Event;
import solutions.fairdata.fdp.index.service.EventService;
import solutions.fairdata.fdp.index.service.WebhookService;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(PingController.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private WebhookService webhookService;

    @Operation(hidden = true)
    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void triggerMetadataRetrieve(@RequestParam(required = false) String clientUrl, HttpServletRequest request) {
        logger.info("Received ping from {}", request.getRemoteAddr());
        final Event event = eventService.acceptAdminTrigger(request, clientUrl);
        webhookService.triggerWebhooks(event);
        eventService.triggerMetadataRetrieval(event);
    }

    @Operation(hidden = true)
    @PostMapping("/ping-webhook")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void webhookPing(@RequestParam(required = true) UUID webhook, HttpServletRequest request) {
        logger.info("Received webhook {} ping trigger from {}", webhook, request.getRemoteAddr());
        final Event event = webhookService.handleWebhookPing(request, webhook);
        webhookService.triggerWebhooks(event);
    }
}

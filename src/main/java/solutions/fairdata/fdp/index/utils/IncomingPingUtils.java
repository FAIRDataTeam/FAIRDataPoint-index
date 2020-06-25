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
package solutions.fairdata.fdp.index.utils;

import org.springframework.http.HttpEntity;
import solutions.fairdata.fdp.index.entity.events.Event;
import solutions.fairdata.fdp.index.entity.events.EventType;
import solutions.fairdata.fdp.index.entity.events.IncomingPing;
import solutions.fairdata.fdp.index.entity.http.Exchange;
import solutions.fairdata.fdp.index.entity.http.ExchangeDirection;

import javax.servlet.http.HttpServletRequest;

public class IncomingPingUtils {

    private static final Integer VERSION = 1;

    public static Event prepareEvent(HttpEntity<String> httpEntity, HttpServletRequest request) {
        var incomingPing = new IncomingPing();
        var ex = new Exchange(ExchangeDirection.INCOMING, request.getRemoteAddr());
        incomingPing.setExchange(ex);
        ex.getRequest().setFromHttpEntity(httpEntity);
        ex.getRequest().setFromHttpServletRequest(request);
        return new Event(VERSION, incomingPing);
    }
}

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
package solutions.fairdata.fdp.index.entity.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import solutions.fairdata.fdp.index.entity.IndexEntry;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "event")
public class Event {
    @Id
    protected ObjectId id;
    @Indexed(unique=true)
    @NotNull
    private UUID uuid = UUID.randomUUID();
    @NotNull
    private EventType type;
    @NotNull
    private Integer version;

    @DBRef
    private Event triggeredBy;
    @DBRef
    private IndexEntry relatedTo;

    // Content (one of those)
    private IncomingPing incomingPing;
    private MetadataRetrieval metadataRetrieval;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant created = Instant.now();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant executed;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant finished;

    public boolean isExecuted() {
        return executed != null;
    }

    public void execute() {
        executed = Instant.now();
    }

    public boolean isFinished() {
        return finished != null;
    }

    public void finish() {
        finished = Instant.now();
    }

    public Event(Integer version, IncomingPing incomingPing) {
        this.type = EventType.IncomingPing;
        this.version = version;
        this.incomingPing = incomingPing;
    }

    public Event(Integer version, Event triggerEvent, IndexEntry relatedTo, MetadataRetrieval metadataRetrieval) {
        this.type = EventType.MetadataRetrieval;
        this.version = version;
        this.triggeredBy = triggerEvent;
        this.relatedTo = relatedTo;
        this.metadataRetrieval = metadataRetrieval;
    }
}
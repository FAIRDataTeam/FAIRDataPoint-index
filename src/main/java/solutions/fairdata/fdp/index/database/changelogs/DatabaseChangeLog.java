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
package solutions.fairdata.fdp.index.database.changelogs;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import solutions.fairdata.fdp.index.entity.IndexEntry;

import java.time.Instant;
import java.time.OffsetDateTime;

@ChangeLog
public class DatabaseChangeLog {
    @ChangeSet(order = "000", id = "initMongoDB", author = "MarekSuchanek")
    public void initMongoDB(MongoDatabase db) {
        // Nothing to DO, just "first" making the version
    }

    @ChangeSet(order = "001", id = "entryTimestampsToInstant", author = "MarekSuchanek")
    public void entryTimestampsToInstant(MongoDatabase db) {
        MongoCollection<Document> indexEntries = db.getCollection("indexEntry");
        for (Document indexEntry : indexEntries.find()) {
            if (!(indexEntry.get("registrationTime") instanceof String)) continue;
            String registrationTimeStr = indexEntry.getString("registrationTime");
            Instant registrationTimeDate = OffsetDateTime.parse(registrationTimeStr).toInstant();
            String modificationTimeStr = indexEntry.getString("modificationTime");
            Instant modificationTimeDate = OffsetDateTime.parse(modificationTimeStr).toInstant();
            indexEntries.updateOne(
                    Filters.eq("_id", indexEntry.getObjectId("_id")),
                    Updates.combine(
                            Updates.set("registrationTime", registrationTimeDate),
                            Updates.set("modificationTime", modificationTimeDate)
                    )
            );
        }
    }
}

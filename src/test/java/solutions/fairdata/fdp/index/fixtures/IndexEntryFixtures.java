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
package solutions.fairdata.fdp.index.fixtures;

import solutions.fairdata.fdp.index.entity.IndexEntry;
import solutions.fairdata.fdp.index.entity.IndexEntryState;
import solutions.fairdata.fdp.index.entity.RepositoryMetadata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IndexEntryFixtures {

    private static IndexEntry newIndexEntry(String clientUrl, Instant timestamp) {
        IndexEntry indexEntry = new IndexEntry();
        indexEntry.setClientUrl(clientUrl);
        indexEntry.setModificationTime(timestamp);
        indexEntry.setRegistrationTime(timestamp);
        indexEntry.setState(IndexEntryState.Invalid);
        return indexEntry;
    }

    public static IndexEntry entryExample() {
        return newIndexEntry("http://example.com", Instant.now());
    }

    public static List<IndexEntry> entriesFew() {
        Instant ref = Instant.now();
        return Arrays.asList(
                newIndexEntry("http://example.com", ref),
                newIndexEntry("http://test.com", ref.minusSeconds(1)),
                newIndexEntry("http://localhost", ref.minusSeconds(2))
        );
    }

    public static List<IndexEntry> entriesN(int n) {
        ArrayList<IndexEntry> entries = new ArrayList<>();
        Instant ref = Instant.now();
        for (int i = 0; i < n; i++) {
            Instant entryTime = ref.minusSeconds(i);
            entries.add(newIndexEntry("http://example" + i + ".com", entryTime));
        }
        return entries;
    }

    public static IndexEntry activeEntry(String clientUrl) {
        IndexEntry indexEntry = new IndexEntry();
        indexEntry.setClientUrl(clientUrl);
        indexEntry.setModificationTime(Instant.now());
        indexEntry.setRegistrationTime(Instant.now());
        indexEntry.setLastRetrievalTime(Instant.now());
        indexEntry.setState(IndexEntryState.Valid);
        indexEntry.setCurrentMetadata(new RepositoryMetadata());
        indexEntry.getCurrentMetadata().setRepositoryUri("http://purl.org/example");
        indexEntry.getCurrentMetadata().setMetadataVersion(1);
        indexEntry.getCurrentMetadata().getMetadata().put("title", "Example FDP");
        indexEntry.getCurrentMetadata().getMetadata().put("version", "1.0.0");
        indexEntry.getCurrentMetadata().getMetadata().put("description", "This is my example FAIR Data Point");
        return indexEntry;
    }
}

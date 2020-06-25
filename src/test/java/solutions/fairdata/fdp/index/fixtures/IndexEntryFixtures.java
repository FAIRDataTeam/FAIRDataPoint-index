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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexEntryFixtures {

    private static final Instant TIMESTAMP = Instant.parse("2020-01-01T00:00:00Z");

    private static IndexEntry newIndexEntry(String clientUrl) {
        IndexEntry indexEntry = new IndexEntry();
        indexEntry.setClientUrl(clientUrl);
        indexEntry.setModificationTime(TIMESTAMP);
        indexEntry.setRegistrationTime(TIMESTAMP);
        return indexEntry;
    }

    public static IndexEntry entryExample() {
        return newIndexEntry("http://example.com");
    }

    public static List<IndexEntry> entriesFew() {
        return Arrays.asList(
                newIndexEntry("http://example.com"),
                newIndexEntry("http://test.com"),
                newIndexEntry("http://localhost")
        );
    }

    public static List<IndexEntry> entriesN(int n) {
        ArrayList<IndexEntry> entries = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            entries.add(newIndexEntry("http://example" + i + ".com"));
        }
        return entries;
    }
}

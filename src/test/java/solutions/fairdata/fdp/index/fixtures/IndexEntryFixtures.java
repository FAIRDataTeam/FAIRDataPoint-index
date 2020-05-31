package solutions.fairdata.fdp.index.fixtures;

import solutions.fairdata.fdp.index.domain.IndexEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexEntryFixtures {

    private static final String TIMESTAMP = "2020-01-01T00:00:00Z";

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

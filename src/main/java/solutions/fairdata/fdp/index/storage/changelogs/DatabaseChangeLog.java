package solutions.fairdata.fdp.index.storage.changelogs;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.client.MongoDatabase;

@ChangeLog
public class DatabaseChangeLog {
    @ChangeSet(order = "000", id = "initMongoDB", author = "MarekSuchanek")
    public void initMongoDB(MongoDatabase db){
        // Nothing to DO, just "first" making the version
    }
}

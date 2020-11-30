package test;

import duid.DuidDatabase;
import duid.ResourceDuidDatabase;
import duid.ResourcesDuidDatabaseContext;
import org.junit.Assert;

public class CheckResourcesDatabase {
    public static void main(String[] args) {
        ResourcesDuidDatabaseContext context = new ResourcesDuidDatabaseContext();
        DuidDatabase database = new ResourceDuidDatabase(context);
        database.pull();
        Assert.assertEquals(1, database.getRoot().getMaps().size());
        Assert.assertEquals(2, database.getRoot().getNamespaces().size());
    }
}

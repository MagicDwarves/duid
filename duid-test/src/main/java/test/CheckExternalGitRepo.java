package test;

import duid.DuidDatabase;
import duid.JGitDuidDatabase;
import duid.JGitDuidDatabaseContext;
import org.eclipse.jgit.api.Git;
import org.junit.Assert;

import java.io.File;

public class CheckExternalGitRepo {
    public static void main(String[] args) {
        File projectBasedir = new File(args[0]);
        try {
            JGitDuidDatabaseContext context = new JGitDuidDatabaseContext();
            context.setBasedir(new File(projectBasedir, "target/externalCheck"));
            context.setOriginUrl(new File(projectBasedir, "target/external").getAbsolutePath());
            DuidDatabase database = new JGitDuidDatabase(context);
            database.pull();
            Assert.assertEquals(1, database.getRoot().getMaps().size());
            Assert.assertEquals(2, database.getRoot().getNamespaces().size());
        } finally {
            Git.shutdown();
        }
    }
}

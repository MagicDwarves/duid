package test;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class PrepareExternalGitRepo {
    public static void main(String[] args) throws GitAPIException {
        File projectBasedir = new File(args[0]);

        File basedir = new File(projectBasedir, "target/external");
        basedir.mkdirs();
        try {
            Git.init().setDirectory(basedir).call().close();
        } finally {
            Git.shutdown();
        }
    }
}

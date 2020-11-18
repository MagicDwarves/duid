package duid;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshTransport;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "update",
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        threadSafe = true)
public class DuidUpdateMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.compileClasspathElements}", readonly = true, required = true)
    private List<String> compileClasspath;

    @Parameter(property = "targetDir", defaultValue = "${project.build.directory}/classes/database", required = true)
    private File targetDir;

    @Parameter(property = "originUrl", required = true)
    private String originUrl;

    @Parameter(property = "originBranch", defaultValue = "master", required = true)
    private String originBranch;

    @Parameter(property = "privateKey")
    private String privateKey;

    @Override
    public void execute() {
        try {
            getLog().info("duid started"
                    + " ,targetDir: " + targetDir
            );

            targetDir.mkdirs();

            JGitDuidDatabaseContext context = new JGitDuidDatabaseContext();
            context.setBasedir(targetDir);
            context.setOriginBranch(originBranch);
            context.setOriginUrl(originUrl);
            if (privateKey != null && !privateKey.isEmpty()) {
                TransportConfigCallback transportConfigCallback = transport -> {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(new CustomConfigSessionFactory(privateKey));
                };
                context.setTransportConfigCallback(transportConfigCallback);
            }
            DuidDatabase database = new JGitDuidDatabase(context);
            ClassLoader pluginClassloader = getClass().getClassLoader();

            List<URL> urls = compileClasspath
                    .stream()
                    .map(File::new)
                    .map(file -> {
                        try {
                            return file.toURI().toURL();
                        } catch (MalformedURLException e) {
                            throw new DuidException("invalidClassPathUrl: " + file, e);
                        }
                    })
                    .collect(Collectors.toList());

            URL[] urlArray = new URL[urls.size()];
            urls.toArray(urlArray);
            URLClassLoader projectClassLoader = new URLClassLoader(urlArray, pluginClassloader);

            DuidService service = new DuidService(database, projectClassLoader);
            service.update();

            getLog().info("duid finished");
        } catch (Exception e) {
            getLog().error("executeError", e);
            throw new DuidException("duidError", e);
        } finally {
            Git.shutdown();
        }

    }

}
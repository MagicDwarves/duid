package duid;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;

public class CustomConfigSessionFactory extends JschConfigSessionFactory {

    private final String prvkey;

    public CustomConfigSessionFactory(String prvkey) {
        this.prvkey = prvkey;
    }

    @Override
    protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
        JSch jsch = super.getJSch(hc, fs);
        jsch.removeAllIdentity();
        jsch.addIdentity(prvkey);
        return jsch;
    }
}

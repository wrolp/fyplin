package org.wrolplin.app;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.NoCloseInputStream;
import org.apache.sshd.common.util.io.NoCloseOutputStream;

public class SshTest {
    
    public static void main(String[] args) {
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        try (ClientSession session = client.connect("cisco", "192.168.1.15", 22)
                .verify()
                .getSession()) {
            session.addPasswordIdentity("cisco");
            session.auth().verify();
            try (ChannelShell shell = session.createShellChannel();
                 InputStream stdin = new NoCloseInputStream(System.in);
                 OutputStream stdout = new NoCloseOutputStream(System.out);
                 OutputStream stderr = new NoCloseOutputStream(System.err)) {
                shell.setUsePty(true);
                shell.setPtyType("xterm");
                shell.setIn(stdin);
                shell.setOut(stdout);
                shell.setErr(stderr);
                shell.open().verify(9L, TimeUnit.SECONDS);
                shell.waitFor(Arrays.asList(ClientChannelEvent.CLOSED), 0);
           }
//            String result = session.executeRemoteCommand("show config");
//            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.stop();
        }
    }
    
}

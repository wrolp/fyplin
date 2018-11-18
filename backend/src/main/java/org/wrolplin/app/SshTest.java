package org.wrolplin.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;

public class SshTest {
    
    private SshClient client;
    private ClientSession session;
    private ChannelShell shell;
    
    private PipedOutputStream toServer;
    private PipedInputStream fromServer;
    
    public SshTest() {
    }
    
    public void test() {
        client = SshClient.setUpDefaultClient();
        client.start();
        try (ClientSession session = client.connect("gns3", "192.168.1.104", 22)
                .verify()
                .getSession()) {
            this.session = session;
            session.addPasswordIdentity("gns3");
            session.auth().verify();
            try (ChannelShell shell = session.createShellChannel();
//                 InputStream stdin = new NoCloseInputStream(System.in);
//                 OutputStream stdout = new NoCloseOutputStream(System.out);
//                 OutputStream stderr = new NoCloseOutputStream(System.err);
                 PipedInputStream inPis = new PipedInputStream();
                 PipedOutputStream inPos = new PipedOutputStream(inPis);
                 PipedInputStream outPis = new PipedInputStream();
                 PipedOutputStream outPos = new PipedOutputStream(outPis)) {
                this.shell = shell;
                shell.setUsePty(true);
                shell.setPtyType("xterm");
                shell.setIn(inPis);
                shell.setOut(outPos);
                toServer = inPos;
                fromServer = outPis;
//                new Sender(outPos).start();
//                new Consumer(inPis).start();
//                shell.setIn(stdin);
//                shell.setOut(stdout);
//                shell.setErr(stderr);
                shell.open().verify(9L, TimeUnit.SECONDS);
                
//                new Thread() {
//                @Override
//                public void run() {
                                 
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(toServer, StandardCharsets.UTF_8));
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(fromServer, StandardCharsets.UTF_8))
                                ) {
//                    for (int i = 0; i < 3; i++) {
                        writer.write("ls");
                        writer.write(0x0d);
                        writer.flush();

                        System.out.println(" +++++++++++++ " + (/*i +*/ 1) + " +++++++++++++ ");

                        String line;
                        while ((line = reader.readLine()) != null) {
//                            line = reader.readLine();
                            System.out.println(line);
                        }
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                }
//                }.start();
                
//                shell.waitFor(Arrays.asList(ClientChannelEvent.CLOSED), 0);
            }
//            String result = session.executeRemoteCommand("show config");
//            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.stop();
        }
    }
    
    class Sender extends Thread {
        
        private OutputStream out;
        
        public Sender(OutputStream out) {
            this.out = out;
            this.setName("Sender");
        }
        
        @Override
        public void run() {
            while (true) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                    writer.write("ls");
                    writer.write(0x0d);
                    writer.flush();
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        
    }
    
    class Consumer extends Thread {
        
        private InputStream in;
        
        public Consumer(InputStream in) {
            this.in = in;
            this.setName("Consumer");
        }
        
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[256];
                int size;
                while (true) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                        String line = reader.readLine();
                        System.out.println(line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    public static void main(String[] args) {
        SshTest ssh = new SshTest();
        ssh.test();
    }
    
}

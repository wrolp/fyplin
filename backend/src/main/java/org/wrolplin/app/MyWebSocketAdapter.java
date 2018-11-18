package org.wrolplin.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.EnumSet;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class MyWebSocketAdapter extends WebSocketAdapter {
    private SshClient client;
    private InputStream fromServer;
    private OutputStream toServer;
    private boolean stopped;

    @Override
    public void onWebSocketConnect(final Session sess) {
        super.onWebSocketConnect(sess);
        new Thread("transfer") {
            public void run() {
                client = SshClient.setUpDefaultClient();
                client.start();
                try (ClientSession session = client.connect("gns3", "192.168.1.104", 22)
                        .verify()
                        .getSession()) {
                    session.addPasswordIdentity("gns3");
                    session.auth().verify();
                    try (ChannelShell channel = session.createShellChannel();
                            PipedInputStream inPis = new PipedInputStream();
                            PipedOutputStream inPos = new PipedOutputStream(inPis);
                            PipedInputStream outPis = new PipedInputStream();
                            PipedOutputStream outPos = new PipedOutputStream(outPis)) {

                        channel.setUsePty(true);
                        channel.setPtyType("xterm");
                        channel.setIn(inPis);
                        channel.setOut(outPos);
                        fromServer = outPis;
                        toServer = inPos;
                        channel.setPtyWidth(40);
                        channel.setPtyHeight(50);
                        channel.open().verify();

                        try {
//                            (
//                             BufferedWriter writer = new BufferedWriter(
//                                new OutputStreamWriter(toServer, StandardCharsets.UTF_8));
//                             BufferedReader reader = new BufferedReader(
//                                new InputStreamReader(fromServer, StandardCharsets.UTF_8))
//                            ) {
//                            System.out.println("write");
//                            writer.write("ls");
//                            writer.write(0x0d);
//                            writer.flush();
//                            System.out.println("writed");

                            byte[] buffer = new byte[1024];
                            int size;
                            while ((size = fromServer.read(buffer)) != -1) {
                                getSession().getRemote().sendBytes(ByteBuffer.wrap(buffer, 0, size));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onWebSocketText(String message) {
        System.out.println("Get message `" + message + "` from " + getSession().getRemoteAddress());
        try {
            toServer.write(message.getBytes());
            toServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        System.out.println("Get binary message from " + getSession().getRemoteAddress());
        try {
            toServer.write(payload, offset, len);
            toServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        try {
            System.out.println(statusCode);
            System.out.println(reason);
            client.close();
            stopped = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

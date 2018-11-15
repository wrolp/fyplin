package org.wrolplin.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        new Thread() {
//            private int count = 0;
            public void run() {
//                while (sess.isOpen()) {
//                    try {
//                        sess.getRemote().sendString("Count " + ++count);
//                        Thread.sleep(15000); // 15 sec
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
                client = SshClient.setUpDefaultClient();
                client.start();
                try (ClientSession session = client.connect("cisco", "192.168.1.15", 22)
                        .verify()
                        .getSession()) {
                    session.addPasswordIdentity("cisco");
                    session.auth().verify();
                    try (ChannelShell channel = session.createShellChannel();
                            PipedInputStream inPis = new PipedInputStream();
                            PipedOutputStream inPos = new PipedOutputStream(inPis);
                            PipedInputStream outPis = new PipedInputStream();
                            PipedOutputStream outPos = new PipedOutputStream(outPis)) {
                        channel.setUsePty(true);
                        channel.setPtyType("xterm");
                        channel.setOut(outPos);
                        channel.setIn(inPis);
                        fromServer = inPis;
                        toServer = outPos;
                        channel.setPtyWidth(40);
                        channel.setPtyHeight(50);
                        channel.open().verify();
                        List<ClientChannelEvent> events = new ArrayList<>();
                        events.add(ClientChannelEvent.CLOSED);
                        channel.waitFor(events, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            public void run() {
                try {
                    byte[] buffer = new byte[1024];
                    int size;
                    stopped = false;
                    while (!stopped) {
                        if (fromServer != null && (size = fromServer.read(buffer)) != -1) {
                            getSession().getRemote().sendBytes(ByteBuffer.wrap(buffer, 0, size));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
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

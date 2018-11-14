package org.wrolplin.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
                try (ClientSession session = client.connect("gns3", "192.168.1.104", 22)
                        .verify()
                        .getSession()) {
                    session.addPasswordIdentity("gns3");
                    session.auth().verify();
                    try (ChannelShell shell = session.createShellChannel()) {
                        fromServer = shell.getIn();
                        toServer = shell.getOut();
                        shell.setPtyWidth(40);
                        shell.setPtyHeight(50);
                        shell.open().verify();
                        toServer.write("ls".getBytes(), 0, 2);
                        List<ClientChannelEvent> events = new ArrayList<>();
                        events.add(ClientChannelEvent.CLOSED);
                        shell.waitFor(events, 0);
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
                    while (fromServer == null) {
                        Thread.sleep(100);
                    }
                    while ((size = fromServer.read(buffer)) != -1) {
                        getSession().getRemote().sendBytes(ByteBuffer.wrap(buffer, 0, size));
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
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        System.out.println("Get binary message from " + getSession().getRemoteAddress());
        try {
            toServer.write(payload, offset, len);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

package org.wrolplin.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.sshd.client.SshClient;
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
//                client = SshClient.setUpDefaultClient();
//                client.start();
//                try (ClientSession session = client.connect("gns3", "192.168.1.104", 22)
//                        .verify()
//                        .getSession()) {
//                    session.addPasswordIdentity("gns3");
//                    session.auth().verify();
//                    try (ChannelShell channel = session.createShellChannel();
//                            PipedInputStream inPis = new PipedInputStream();
//                            PipedOutputStream inPos = new PipedOutputStream(inPis);
//                            PipedInputStream outPis = new PipedInputStream();
//                            PipedOutputStream outPos = new PipedOutputStream(outPis)) {
//
//                        channel.setUsePty(true);
//                        channel.setPtyType("xterm");
//                        channel.setIn(inPis);
//                        channel.setOut(outPos);
//                        fromServer = outPis;
//                        toServer = inPos;
//                        channel.setPtyWidth(40);
//                        channel.setPtyHeight(50);
//                        channel.open().verify();
//
//                        try {
////                            (
////                             BufferedWriter writer = new BufferedWriter(
////                                new OutputStreamWriter(toServer, StandardCharsets.UTF_8));
////                             BufferedReader reader = new BufferedReader(
////                                new InputStreamReader(fromServer, StandardCharsets.UTF_8))
////                            ) {
////                            System.out.println("write");
////                            writer.write("ls");
////                            writer.write(0x0d);
////                            writer.flush();
////                            System.out.println("writed");
//
//                            byte[] buffer = new byte[1024];
//                            int size;
//                            while ((size = fromServer.read(buffer)) != -1) {
//                                getSession().getRemote().sendBytes(ByteBuffer.wrap(buffer, 0, size));
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                        channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                SimpleDateFormat formatter = new SimpleDateFormat("YYYYMMDDHHmmss");
                String path = "F:/wrolp/session/" + formatter.format(new Date());
                File file = new File(path);
                try {
                    new File("F:/wrolp/session").mkdirs();  
                    file.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try (FileOutputStream toFile = new FileOutputStream(file)) {
                    List<String> cmd = new ArrayList<>();
                    cmd.add("F:/wrolp/putty/PLINK");
                    cmd.add("-v");
                    cmd.add("-a");
                    cmd.add("-x");
                    cmd.add("-l");
                    cmd.add("cisco");
                    cmd.add("-pw");
                    cmd.add("cisco");
                    cmd.add("-1");
                    cmd.add("-P");
                    cmd.add("22");
                    cmd.add("192.168.1.15");
                    ProcessBuilder ssh = new ProcessBuilder(cmd);
                    ssh.redirectErrorStream(true);
                    Process plink = ssh.start();
                    toServer = plink.getOutputStream();
                    fromServer = plink.getInputStream();
                    byte[] buffer = new byte[1024];
                    int size;
                    while ((size = fromServer.read(buffer)) != -1) {
                        getSession().getRemote().sendBytes(ByteBuffer.wrap(buffer, 0, size));
                        toFile.write(buffer, 0, size);
                        toFile.flush();
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

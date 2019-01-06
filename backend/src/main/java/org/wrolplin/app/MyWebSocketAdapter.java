package org.wrolplin.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.xml.XmlConfiguration;

public class MyWebSocketAdapter extends WebSocketAdapter {
//    private SshClient client;
    private InputStream fromServer;
    private OutputStream toServer;
//    private boolean stopped;
    private WebSocketAdapter adapter = this;
    private Thread transfer;
//    private Process plink;
    private TerminalClient client;

    private long timestamp;
    private long timeout;

    @Override
    public void onWebSocketConnect(final Session sess) {
        super.onWebSocketConnect(sess);

        transfer = new Thread("transfer") {
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

                PlinkClientBuilder builder = new PlinkClientBuilder();
                // CHECKSTYLE:OFF
                builder.withDirectory("F:/wrolp/fyplin/assemblies/karaf/src/main/resources/bin/putty")
                .withVerbose(true)
                .withX11ForwardingDisable()
                .withAgentForwardingDisable()
                .withHost("192.168.1.15")
                .withSshV1()
                .withPort(22)
                .withUser("cisco")
                .withPassword("cisco");
                // CHECKSTYLE:ON
                client = builder.build();

                try (FileOutputStream toFile = new FileOutputStream(file, true)) {
                    client.start(true);
                    fromServer = client.getInputStream();
                    toServer = client.getOutputStream();

//                    List<String> cmd = new ArrayList<>();
//                    cmd.add("F:/wrolp/fyplin/assemblies/karaf/src/main/resources/bin/putty/PLINK");
//                    cmd.add("-v");
//                    cmd.add("-a");
//                    cmd.add("-x");
//                    cmd.add("-l");
//                    cmd.add("cisco");
//                    cmd.add("-pw");
//                    cmd.add("cisco");
////                    cmd.add("-1");
//                    cmd.add("-P");
//                    cmd.add("22");
////                    int suffix = new Random().nextInt(999);
////                    cmd.add("-sshlog");
////                    cmd.add("F:/wrolp/session/192.168.1.104-log-" + suffix);
////                    cmd.add("-sshrawlog");
////                    cmd.add("F:/wrolp/session/192.168.1.15-rawlog-" + suffix);
//
//                    cmd.add("192.168.1.15");
//                    ProcessBuilder ssh = new ProcessBuilder(cmd);
//                    ssh.redirectErrorStream(false);
//                    plink = ssh.start();
////                    plink.waitFor(10, TimeUnit.MINUTES);
//                    toServer = plink.getOutputStream();
////                    WritableByteChannel toServerChannel = Channels.newChannel(toServer);
//                    fromServer = plink.getInputStream();
////                    fromServer = plink.getErrorStream();
                    byte[] buffer = new byte[1024];
                    int size;
                    while ((size = fromServer.read(buffer)) != -1) {
                        getSession().getRemote().sendBytes(ByteBuffer.wrap(buffer, 0, size));
                        toFile.write(buffer, 0, size);
                        toFile.flush();

                        for (int i = 0; i < size; i++) {
                            byte b = buffer[i];
                            if (b == ' ') {
//                                System.out.print("[SP]");
                                System.out.print(" ");
                            } else if (b == '\t') {
                                System.out.print("[TAB]");
                            } else if (b >= 32 && b <= 126 || b == '\r' || b == '\n') {
                                System.out.print((char) b);
                            }
                            else if (b == 0x1B) {
                                System.out.print("[ESC]");
                            }
                            else if (b == 0x08) {
                                System.out.print("[BS]");
                            }
                            else if (b == 0x07) {
                                System.out.print("[BEL]");
                            }
                            else {
                                System.out.print("[" + Integer.toHexString(b) + "]");
                            }
//                            System.out.print((char) b);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    if (plink.isAlive()) {
//                        plink.destroy();
//                    }
                    client.close();
                    System.out.println("[Closed]");
//                    System.out.println(plink.exitValue());
                    sess.close();
                }
            }
        };

//        WebSocketSession session = (WebSocketSession) sess;
//      WebSocketRemoteEndpoint remote = (WebSocketRemoteEndpoint) session.getRemote();
//        WebSocketServerConnection connection = (WebSocketServerConnection) session.getConnection();
//        connection.tryFillInterested(new Callback() {
//            public void failed(Throwable x) {
//                x.printStackTrace();
//                transfer.interrupt();
//            }
//        });
        transfer.start();
    }

    @Override
    public void onWebSocketText(String message) {
//        System.out.println("Get message `" + message + "` from " + getSession().getRemoteAddress());
        System.out.println("\n[SENDING] " + message);
        try {
            toServer.write(message.getBytes());
            toServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
//        System.out.println("Get binary message from " + getSession().getRemoteAddress());
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
//        if (transfer != null) {
//            try {
//                fromServer.close();
//                toServer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if (plink != null) {
//            plink.destroy();
//        }
        if (client != null) client.close();
        System.out.println("\nWebSocket is closed\n[" + statusCode + "] " + reason);
//        try {
//            client.close();
//            stopped = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            InputStream is = Main.class.getResourceAsStream("/jetty.xml");
            XmlConfiguration config = new XmlConfiguration(is);
            config.configure(server);
            // ------------
//            ContextHandler handler = new ContextHandler();
//            handler.setContextPath("/");
//            handler.setHandler(new MyWebSocketHandler());
//            server.setHandler(handler);
            // ------------
//            ServletContextHandler sch = new ServletContextHandler(ServletContextHandler.SESSIONS);
//            sch.setContextPath("/");
//            ServletHolder sh = new ServletHolder("websocket", new MyWebSocketServlet());
//            sch.addServlet(sh, "/ws");
            // ------------
            server.start();
            System.out.println("!!! Jetty Server Started !!!");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

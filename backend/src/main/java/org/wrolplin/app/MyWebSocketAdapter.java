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
    private InputStream fromServer;
    private InputStream errorInput;
    private OutputStream toServer;
    // private boolean stopped;
    private Thread transfer;
    private Thread errorHandler;
    private TerminalClient client;

    // private long timestamp;
    // private long timeout;

    @Override
    public void onWebSocketConnect(final Session sess) {
        super.onWebSocketConnect(sess);

        transfer = new Thread("transfer") {
            public void run() {
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
                    client.start(false);
                    fromServer = client.getInputStream();
                    errorInput = client.getErrorStream();
                    toServer = client.getOutputStream();

                    byte[] buffer = new byte[1024];
                    int size;
                    while ((size = fromServer.read(buffer)) != -1) {
                        getSession().getRemote().sendBytes(ByteBuffer.wrap(buffer, 0, size));
                        toFile.write(buffer, 0, size);
                        toFile.flush();

                        for (int i = 0; i < size; i++) {
                            byte b = buffer[i];
                            if (b == ' ') {
                                // System.out.print("[SP]");
                                System.out.print(" ");
                            } else if (b == '\t') {
                                System.out.print("[TAB]");
                            } else if (b >= 32 && b <= 126 || b == '\r' || b == '\n') {
                                System.out.print((char) b);
                            } else if (b == 0x1B) {
                                System.out.print("[ESC]");
                            } else if (b == 0x08) {
                                System.out.print("[BS]");
                            } else if (b == 0x07) {
                                System.out.print("[BEL]");
                            } else {
                                System.out.print("[" + Integer.toHexString(b) + "]");
                            }
                            // System.out.print((char) b);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    client.close();
                    System.out.println("[Closed]");
                    sess.close();
                }
            }
        };

        errorHandler = new Thread(() -> {
            while (errorInput == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            if (errorHandler != null) {
                byte[] buffer = new byte[1024];
                int size;
                try {
                    while ((size = errorInput.read(buffer)) != -1) {
                        for (int i = 0; i < size; i++) {
                            byte b = buffer[i];
                            if (b == ' ') {
                                System.out.print(" ");
                            } else if (b == '\t') {
                                System.out.print("[TAB]");
                            } else if (b >= 32 && b <= 126 || b == '\r' || b == '\n') {
                                System.out.print((char) b);
                            } else if (b == 0x1B) {
                                System.out.print("[ESC]");
                            } else if (b == 0x08) {
                                System.out.print("[BS]");
                            } else if (b == 0x07) {
                                System.out.print("[BEL]");
                            } else {
                                System.out.print("[" + Integer.toHexString(b) + "]");
                            }
                            // System.out.print((char) b);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        errorHandler.setName("ErrorHandler");

        transfer.start();
        errorHandler.start();
    }

    @Override
    public void onWebSocketText(String message) {
        // System.out.println("Get message `" + message + "` from " +
        // getSession().getRemoteAddress());
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
        // System.out.println("Get binary message from " + getSession().getRemoteAddress());
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
        if (client != null)
            client.close();
        System.out.println("\nWebSocket is closed\n[" + statusCode + "] " + reason);
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            InputStream is = Main.class.getResourceAsStream("/jetty.xml");
            XmlConfiguration config = new XmlConfiguration(is);
            config.configure(server);
            // ------------
            // ContextHandler handler = new ContextHandler();
            // handler.setContextPath("/");
            // handler.setHandler(new MyWebSocketHandler());
            // server.setHandler(handler);
            // ------------
            // ServletContextHandler sch = new
            // ServletContextHandler(ServletContextHandler.SESSIONS);
            // sch.setContextPath("/");
            // ServletHolder sh = new ServletHolder("websocket", new MyWebSocketServlet());
            // sch.addServlet(sh, "/ws");
            // ------------
            server.start();
            System.out.println("!!! Jetty Server Started !!!");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

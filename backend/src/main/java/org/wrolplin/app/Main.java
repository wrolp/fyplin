package org.wrolplin.app;

import java.io.InputStream;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.xml.XmlConfiguration;

public class Main {
    
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

package org.wrolplin.app;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class MyWebSocketListener implements WebSocketListener {

    private Session session;

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        System.out.println("Session `" + session.getRemoteAddress() +
                "` being closed, Status code: " + statusCode + ", Reason: " + reason);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        System.out.println("New WebSocket connection from " + session.getRemoteAddress());
        this.session = session;
        
        new Thread() {
            public void run() {
                int count = 0;
                while (session.isOpen()) {
                    try {
                        session.getRemote().sendString("Count " + ++count);
                        Thread.sleep(15000); // 15 sec
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        System.out.println("....");
    }

    @Override
    public void onWebSocketText(String message) {
        System.out.println("Get message `" + message + "` from " + session.getRemoteAddress());
    }

}

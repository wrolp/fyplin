package org.wrolplin.app;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class MyWebSocketAdapter extends WebSocketAdapter {
    
    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        new Thread() {
            private int count = 0;
            public void run() {
                while (sess.isOpen()) {
                    try {
                        sess.getRemote().sendString("Count " + ++count);
                        Thread.sleep(15000); // 15 sec
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onWebSocketText(String message) {
        System.out.println("Get message `" + message + "` from " + getSession().getRemoteAddress());
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        System.out.println("Get binary message from " + getSession().getRemoteAddress());
    }

}

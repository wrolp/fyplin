package org.wrolplin.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TerminalClient {

    void start(boolean redirectErr) throws IOException;

    OutputStream getOutputStream();

    InputStream getErrorStream();

    InputStream getInputStream();

    void close();

}

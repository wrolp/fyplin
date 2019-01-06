package org.wrolplin.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PlinkClientBuilder {
    private ProcessBuilder processBuilder;

    private String directory;
    private String plinkAlias;
    private boolean verbose;
    private Protocol protocol = Protocol.ssh;
    private String host;
    private Integer port;
    private String user;
    private String password;
    private boolean enableX11Forwarding = false;
    private boolean disableX11Forwarding = false;
    private boolean enableAgentForwarding = false;
    private boolean disableAgentForwarding = false;
    private boolean enablePtyAllocation = false;
    private boolean disablePtyAllocation = false;
    private boolean compression;
    private boolean enablePageant = false;
    private boolean disablePageant = false;
    private boolean sshV1 = false;
    private boolean sshV2 = false;
    private boolean ipV4 = false;
    private boolean ipV6 = false;

    public PlinkClientBuilder withDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    public PlinkClientBuilder withPlinkAlias(String alias) {
        this.plinkAlias = alias;
        return this;
    }

    /**
     * whether show verbose messages of plink or not, plink option: <b>-v</b> will show verbose
     * messages
     * @param verbose
     * @return
     */
    public PlinkClientBuilder withVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    /**
     * force use of a particular protocol, default to ssh
     * @param protocol
     * @return
     */
    public PlinkClientBuilder withProtocol(Protocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public PlinkClientBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public PlinkClientBuilder withPort(Integer port) {
        this.port = port;
        return this;
    }

    public PlinkClientBuilder withUser(String user) {
        this.user = user;
        return this;
    }

    public PlinkClientBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public PlinkClientBuilder withAgentForwardingEnable() {
        this.enableAgentForwarding = true;
        return this;
    }

    public PlinkClientBuilder withAgentForwardingDisable() {
        this.disableAgentForwarding = true;
        return this;
    }

    public PlinkClientBuilder withX11ForwardingEnable() {
        this.enableX11Forwarding = true;
        return this;
    }

    public PlinkClientBuilder withX11ForwardingDisable() {
        this.disableX11Forwarding = true;
        return this;
    }

    public PlinkClientBuilder withPtyAllocationEnable() {
        this.enablePtyAllocation = true;
        return this;
    }

    public PlinkClientBuilder withPtyAllocationDisable() {
        this.disablePtyAllocation = true;
        return this;
    }

    public PlinkClientBuilder withCompression() {
        this.compression = true;
        return this;
    }

    public PlinkClientBuilder withPageantEnable() {
        this.enablePageant = true;
        return this;
    }

    public PlinkClientBuilder withPageantDisable() {
        this.disablePageant = true;
        return this;
    }

    /**
     * ssh v1, plink option: <b>-1</b>
     * @return
     */
    public PlinkClientBuilder withSshV1() {
        this.sshV1 = true;
        return this;
    }

    /**
     * ssh v2, plink option: <b>-2</b>
     * @return
     */
    public PlinkClientBuilder withSshV2() {
        this.sshV2 = true;
        return this;
    }

    /**
     * force use of IPv4, plink option: <b>-4</b>
     * @return
     */
    public PlinkClientBuilder withIpV4() {
        this.ipV4 = true;
        return this;
    }

    /**
     * force use of IPv6, plink option: <b>-6</b>
     * @return
     */
    public PlinkClientBuilder withIpV6() {
        this.ipV6 = true;
        return this;
    }

    public PlinkClient build() {
        if (host == null) {
            throw new IllegalArgumentException("host is required");
        }

        processBuilder = new ProcessBuilder();
        String command = plinkAlias != null ? plinkAlias : "plink";
        if (directory != null) {
            File dir = new File(directory);
            if (dir.exists() && dir.isDirectory()) {
                directory = dir.getAbsolutePath();
                processBuilder.directory(dir);
                command = dir.getAbsolutePath() + '/' + command;
            }
        }
        List<String> cmd = new ArrayList<>();
        cmd.add(command);
        // protocol
        cmd.add("-" + protocol.toString());
        // verbose
        if (verbose) {
            cmd.add("-v");
        }
        // user
        if (user != null) {
            cmd.add("-l");
            cmd.add(user);
        }
        // port
        if (port != null && port > 0 && port < 65535) {
            cmd.add("-P");
            cmd.add(port.toString());
        }

        // ssh only
        if (protocol == Protocol.ssh) {
            // password
            if (password != null) {
                cmd.add("-pw");
                cmd.add(password);
            }

            // x11 forwarding
            if (enableX11Forwarding && !disableX11Forwarding) {
                cmd.add("-X");
            }
            if (disableX11Forwarding && !enableX11Forwarding) {
                cmd.add("-x");
            }

            // agent forwarding
            if (enableAgentForwarding && !disableAgentForwarding) {
                cmd.add("-A");
            }
            if (disableAgentForwarding && !enableAgentForwarding) {
                cmd.add("-a");
            }

            // pty allocation
            if (enablePtyAllocation && !disablePtyAllocation) {
                cmd.add("-t");
            }
            if (disablePtyAllocation && !enablePtyAllocation) {
                cmd.add("-T");
            }

            // ssh version
            if (sshV1 && !sshV2) {
                cmd.add("-1");
            }
            if (sshV2 && !sshV1) {
                cmd.add("-2");
            }

            // ip version
            if (ipV4 && !ipV6) {
                cmd.add("-4");
            }
            if (ipV6 && !ipV4) {
                cmd.add("-6");
            }

            // compression
            if (compression) {
                cmd.add("-C");
            }

            // pageant
            if (enablePageant && !disablePageant) {
                cmd.add("-agent");
            }
            if (disablePageant && !enablePageant) {
                cmd.add("-noagent");
            }
        }

        // host
        cmd.add(host);

        processBuilder.command(cmd);
        return new PlinkClient();
    }

    enum Protocol {
        ssh, telnet, rlogin, raw, serial
    }

    class PlinkClient implements TerminalClient {
        private Process plink;

        private InputStream input;
        private InputStream error;
        private OutputStream output;

        @Override
        public void start(boolean redirectErr) throws IOException {
            if (plink != null) return;
            processBuilder.redirectErrorStream(redirectErr);
            plink = processBuilder.start();
            input = plink.getInputStream();
            output = plink.getOutputStream();
            error = plink.getErrorStream();
        }

        @Override
        public OutputStream getOutputStream() {
            checkState();
            return output;
        }

        @Override
        public InputStream getErrorStream() {
            checkState();
            return error;
        }

        @Override
        public InputStream getInputStream() {
            checkState();
            return input;
        }
        
        private void checkState() {
            if (plink == null) throw new IllegalStateException("Plink not started");
        }

        @Override
        public void close() {
            if (plink != null) {
                plink.destroy();
            }
        }

    }

}

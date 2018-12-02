package org.wrolplin.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SshTest2 {

    public static void main(String[] args) {
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
        
        try {
            ssh.redirectErrorStream(true);
            Process plink = ssh.start();
//            plink.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

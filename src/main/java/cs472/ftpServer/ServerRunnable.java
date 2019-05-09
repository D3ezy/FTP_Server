/*
 * Server.java
 * 
 * Author: Matthew Dey
 * Date Created: May 3nd, 2019
 * Drexel University
 * CS 472 - HW3 - Computer Networks
 * 
 */

package cs472.ftpServer;

import java.util.HashMap;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;

public class ServerRunnable implements Runnable {

    private Logger LOGGER;
    private final HashMap<Integer, String> response_codes = new HashMap<>();
    private ServerSocket dataSocket;
    private int dataPort;
    protected Socket clientSocket;
    private InputStream in;
    private OutputStream out;
    private File users;

    ServerRunnable(Socket clientSocket, Logger serverLog) {
        LOGGER = serverLog;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            String cmd = "";
            this.in  = clientSocket.getInputStream();
            this.out = clientSocket.getOutputStream();
            LOGGER.log("Recevied streams. Adding connection to a new thread.");
            this.parseCmds();
            in.close();
            out.close();
        } catch (IOException e) {
            LOGGER.log(e.toString());
        }
    }

    public void openDataPort() {
        return;
    }

    public void readInput() {
        return;
    }

    public void sendResponse() {
        return;
    }

    public void authenticate(String user, String pass) {
        // iterate through file and match username to password
        return;
    }

    public void parseCmds() {
        String cmd = "";
        switch(cmd.toUpperCase()) {
            case "USER":
            case "PASS":
            case "CWD":
            case "CDUP":
            case "QUIT":
            case "PASV":
            case "EPSV":
            case "PORT":
            case "EPRT":
            case "RETR":
            case "STOR":
            case "PWD":
            case "SYST":
            case "LIST":
            case "HELP":
            default:
        }
    }
}
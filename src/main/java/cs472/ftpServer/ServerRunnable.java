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

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class ServerRunnable extends Thread {

    private Logger LOGGER;
    private boolean userLoggedIn;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ServerSocket dataSocket;
    private int dataPort;
    protected Socket clientSocket;

    ServerRunnable(Socket clientSocket, DataInputStream is, DataOutputStream os, Logger serverLog) {
        this.LOGGER = serverLog;
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(is));
        this.writer = new BufferedWriter(new OutputStreamWriter(os));
        this.userLoggedIn = false;
    }

    @Override
    public void run() {
        // print message
        this.sendResponse("220 Connected. DeezyFTP - Welcome!");
        String client_response = this.readInput();
        // wait for response
        // response goes into "parseCmds()"
        this.parseCmds(client_response);
    }

    public String readInput() {
        String read;
        try {
            read = reader.readLine();
            return read;
        } catch(IOException e) {
            LOGGER.log("Unable to read input from Client: " + e.toString());
        }
        return null;
    }

    public void sendResponse(String comm) {
        try {
            writer.write(comm + "\r\n");
            writer.flush();
        } catch (IOException e) {
            LOGGER.log("Unable to write response to client " + this.clientSocket + ". Closing socket.");
        }
        return;
    }

    private void authenticate(String user, String pass) {
        // iterate through file and match username to password
        String auth = user + " " + pass;
        try {
            FileReader fr = new FileReader("./users.txt");
        } catch (FileNotFoundException e) {
            LOGGER.log("Unable to find authenticated users file.");
        }
        return;
    }

    public void parseCmds(String cmd) {
        String[] args = cmd.split(" ");
        switch(args[0].toUpperCase()) {
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
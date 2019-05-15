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
    private String username;

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
        while(true) {
            String client_response = this.readInput();
            // wait for response
            // response goes into "parseCmds()"
            this.parseCmds(client_response);
        }
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

    private void user(String username) {
        this.username = username;
        LOGGER.log("331 Password required for " + username);
        this.sendResponse("331 Password required for " + username);
        return;
    }

    private void pass(String password) {
        if (this.username == null) {
            LOGGER.log("No username provided.");
            LOGGER.log("");
            this.sendResponse("");
            return;
        }
        LOGGER.log("Received username and password. Authenticating.");
        this.authenticate(this.username, password);
        return;
    }

    private void authenticate(String user, String pass) {
        // iterate through file and match username to password
        String auth = user + " " + pass;
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader("./users.txt"));
            while((line = br.readLine()) != null)  {
                if (line.equals(auth)) {
                    this.userLoggedIn = true;
                    this.sendResponse("230 User " + this.username + " logged in");
                    return;
                }
            }
            LOGGER.log("530 Login incorrect.");
            this.sendResponse("530 Login incorrect.");
        } catch (FileNotFoundException e) {
            LOGGER.log("Unable to find authenticated users file.");
        } catch (IOException x) {

        }
        return;
    }

    public void cwd(String directory) {
        return;
    }

    public void cdup() {

    }

    public void quit() {
        return;
    }

    public void pasv() {
        return;
    }

    public void epsv() {
        return;
    }

    public void port(String address) {
        return;
    }

    public void eprt(String address) {
        return;
    }

    public void retr(String file) {
        return;
    }

    public void stor(String filename) {
        return;
    }

    public void pwd() {
        return;
    }

    public void syst() {
        return;
    }

    public void list(String directory) {
        return;
    }

    public void help() {
        return;
    }

    public void parseCmds(String cmd) {
        String[] args = cmd.split(" ");
        switch(args[0].toUpperCase()) {
            case "USER":
                this.user(cmd);
                break;
            case "PASS":
                this.pass(cmd);
                break;
            case "CWD":
                this.cwd(cmd);
                break;
            case "CDUP":
                this.cdup();
                break;
            case "QUIT":
                this.quit();
                break;
            case "PASV":
                this.pasv();
                break;
            case "EPSV":
                this.epsv();
                break;
            case "PORT":
                this.port(cmd);
                break;
            case "EPRT":
                this.eprt(cmd);
                break;
            case "RETR":
                this.retr(cmd);
                break;
            case "STOR":
                this.stor(cmd);
                break;
            case "PWD":
                this.pwd();
                break;
            case "SYST":
                this.syst();
                break;
            case "LIST":
                this.list("");
                this.list(cmd);
                break;
            case "HELP":
                this.help();
                break;
            default:
        }
    }
}
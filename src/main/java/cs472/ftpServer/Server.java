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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Server {

    private Logger LOGGER;
    private boolean isRunning;
    private int connectionPort;
    private ServerSocket connectionSocket;
    protected Thread myThread;

    Server(String logfile, String port) {
        LOGGER = new Logger(logfile);
        try {
            this.connectionPort = Integer.parseInt(port);
            LOGGER.log("Spinning up server instance on localhost:" + port);
        } catch (NumberFormatException e) {
            LOGGER.log("Server initialization: Invalid port, cannot convert String to int.");
            LOGGER.log(e.toString());
        }        
    }
    
    public void run() {
        try {
            this.connectionSocket = new ServerSocket(connectionPort);
            this.isRunning = true;
            while(true) {
                Socket newConn = null;
                LOGGER.log("Accepting new connections...");
                newConn = this.connectionSocket.accept();
                LOGGER.log("A new client has connected: " + newConn);
                DataInputStream input = new DataInputStream(newConn.getInputStream());
                DataOutputStream output = new DataOutputStream(newConn.getOutputStream());
                Thread t = new ServerRunnable(newConn,input,output,LOGGER);
                LOGGER.log("New thread created for client connection: " + newConn);
                t.start();
            }
        } catch (IOException e) {
            LOGGER.log("Failed to create new instance or accept connection: " + e.toString());
        }
    }

    public synchronized void stop() {
        this.isRunning = false;
        try {
            this.connectionSocket.close();
        } catch (IOException e) {
            LOGGER.log("Failed to stop server: " + e.toString());
        }
        
    }
}
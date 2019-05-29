/*
 * ServerRunnable.java
 * 
 * Author: Matthew Dey
 * Date Created: May 3nd, 2019
 * Drexel University
 * CS 472 - HW3 - Computer Networks
 * 
 */

 /*
    Testing info:
    Host: localhost
    Port: 21 (for HW2 Client)
 */

package cs472.ftpServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class ServerRunnable extends Thread {

    private Logger LOGGER;
    private String currDir;
    private boolean userLoggedIn;
    private BufferedReader reader;
    private BufferedWriter writer;
    private BufferedWriter dataWriter;
    private String portHost;
    private int dataPort;
    private int portDataPort;
    protected Socket clientSocket;
    private String username;
    private String thread_id;
    private boolean inPassive;
    private boolean inPort;
    private boolean isRunning;
    private boolean inExtendedPort;
    private boolean inExtendedPassive;
    private int transferMode;

    ServerRunnable(Socket clientSocket, DataInputStream is, DataOutputStream os, Logger serverLog, int mode) {
        this.LOGGER = serverLog;
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(is));
        this.writer = new BufferedWriter(new OutputStreamWriter(os));
        this.userLoggedIn = false;
        this.currDir = System.getProperty("user.dir");
        this.inPassive = false;
        this.inPort = false;
        this.inExtendedPort = false;
        this.inExtendedPassive = false;
        this.isRunning = true;
        this.transferMode = mode;
    }

    @Override
    public void run() {
        // print message
        this.sendResponse("220 Connected. DeezyFTP - Welcome!");
        this.thread_id = Thread.currentThread().getName();
        while(this.isRunning) {
            String client_response = this.readInput();
            // wait for response
            // response goes into "parseCmds()"
            this.parseCmds(client_response);
        }
        return;
    }

    public String readInput() {
        String read;
        try {
            read = reader.readLine();
            return read;
        } catch(IOException e) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to read input from Client: " + e.toString());
        }
        return null;
    }

    public void sendResponse(String comm) {
        try {
            writer.write(comm + "\r\n");
            writer.flush();
        } catch (IOException e) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to write response to client " + this.clientSocket + ". Closing socket.");
        }
        return;
    }

    private void sendData(String comm) {
        try {
            dataWriter.write(comm + "\r\n");
            dataWriter.flush();
        } catch (IOException e) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to write response to client " + this.clientSocket + ". Closing socket.");
        }
        return;
    }
    

    private void user(String username) {
        this.username = username;
        LOGGER.log("[ON: " + this.thread_id + "] " + "331 Password required for " + username);
        this.sendResponse("331 Please specify the password.");
        return;
    }

    private void pass(String password) {
        if (this.username == null) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "No username provided.");
            LOGGER.log("");
            this.sendResponse("");
            return;
        }
        LOGGER.log("[ON: " + this.thread_id + "] " + "Received username and password. Authenticating.");
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
                    this.sendResponse("230 Login successful.");
                    br.close();
                    return;
                }
            }
            LOGGER.log("[ON: " + this.thread_id + "] " + "530 Login incorrect.");
            this.sendResponse("530 Login incorrect.");
            br.close();
        } catch (FileNotFoundException e) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to find authenticated users file.");
        } catch (IOException x) {
        }
        return;
    }

    public void cwd(String directory) {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to change working directory. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }

        // check to see if given directory is valid

        LOGGER.log("CWD cmd: Changing directory to " + this.currDir + "/" + directory);
        // Take directory and add it to the current directory
        String newDirectory = this.currDir + "/" + directory;
        // update current directory when finished
        this.currDir = newDirectory;
        LOGGER.log("[ON: " + this.thread_id + "] " + "CWD cmd: Directory change completed. New working directory: " + this.currDir);
        LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 250 CWD command successful");
        this.sendResponse("250 CWD command successful");
        return;
    }

    public void cdup() {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to cdup. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }

        // get the current directory
        // recursively call cwd to traverse up
        String traversed = this.currDir.substring(0, this.currDir.lastIndexOf('\\'));
        this.cwd(traversed);
        return;
    }

    public void quit() {
        LOGGER.log("Attempting to kill thread, closing all available connections.");
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            LOGGER.log("One or more connections already closed. Exiting.");
        }
        this.isRunning = false;
        return;
    }

    // grab an available port, open data socket on that port, accept connection
    // ez gg
    public void pasv() {

        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "cmd pasv: Unable to enter passive mode. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }

        // if pasv mode is not enabled, let the client know and return
        if (this.transferMode == 2) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: PASV not available with this config.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 502 Command not implemented on this configuration.");
            this.sendResponse("502 Command not implemented on this configuration.");
            return;
        }

        if(this.inPort || this.inExtendedPassive || this.inExtendedPort) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: Client currently connected for PORT/EPRT/EPSV data transfers. Turning off PORT mode.");
            this.inPort = false;
            this.inExtendedPassive = false;
            this.inExtendedPort = false;
            LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: PORT/EPRT/EPSV mode turned off. Ready to enter Passive mode.");
        }
        // test, but get local IP here and store in pasv_cmd
        String pasv_cmd = "127,0,0,1";
        LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: Got IP Address. Added to string.");
        LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: Parsing randomly selected port. Adding to string.");
        ArrayList<Integer> parsed_ports = this.getPasvPort();
        int port_1 = parsed_ports.get(0);
        int port_2 = parsed_ports.get(1);
        pasv_cmd += ","+ port_1 + "," + port_2;
        LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd:  Command created. Sending: " + pasv_cmd);
        LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 227 Entering Passive Mode (" + pasv_cmd + ").");
        this.sendResponse("227 Entering Passive Mode (" + pasv_cmd + ").");

        // set the data connections to prepare for passive transfers
        this.dataPort = parsed_ports.get(2);
        if (!this.inPassive) {
            this.inPassive = true;
        }
        return;
    }

    public void epsv() {

        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "cmd epsv: Unable to enter extended passive mode. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }

        // if epsv mode is not enabled, let the client know and return
        if (this.transferMode == 2) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "EPSV cmd: EPSV not available with this config.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 502 Command not implemented on this configuration.");
            this.sendResponse("502 Command not implemented on this configuration.");
            return;
        }

        if(this.inPort || this.inPassive || this.inExtendedPort) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: Client currently connected for PORT/PASV/EPRT data transfers. Turning off PORT mode.");
            this.inPort = false;
            this.inPassive = false;
            this.inExtendedPort = false;
            LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: PORT/PASV/EPRT mode turned off. Ready to enter Passive mode.");
        }

        LOGGER.log("[ON: " + this.thread_id + "] " + "EPSV cmd: Getting Passive Port.");
        int port = this.getEpsvPort();
        // parse epsv and send command
        // add epsv as an option to list, stor, retr
        // set the data connections to prepare for passive transfers
        LOGGER.log("[ON: " + this.thread_id + "] " + "EPSV cmd: Got port. Setting variables and preparing to send to client.");
        String cmd = "(|||" + port + "|)";
        if (!this.inExtendedPassive) {
            this.inExtendedPassive = true;
        }
        LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 200 Entering Extended Passive Mode " + cmd);
        this.sendResponse("200 Entering Extended Passive Mode " + cmd);
        this.dataPort = port;
        LOGGER.log("[ON: " + this.thread_id + "] " + "EPSV cmd: In Extended PASSIVE mode.");
        return;
    }

    public void port(String address) {

        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Unable to enable port mode. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }

        // if port mode is not enabled, let the client know and return
        if (this.transferMode == 1) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: PORT not available with this config.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 502 Command not implemented on this configuration.");
            this.sendResponse("502 Command not implemented on this configuration.");
            return;
        }

        // if its already in passive mode, turn off
        if(this.inPassive || this.inExtendedPassive || this.inExtendedPort) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Client currently connected for Passive/EPSV/EPRT data transfers. Turning off Port mode.");
            this.inPassive = false;
            this.inExtendedPassive = false;
            this.inExtendedPort = false;
            LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Passive/EPSV/EPRT mode turned off. Ready to enter Port mode.");
        }

        // parse command to get host and port number
        this.parseClientPort(address);

        // return and wait to see what happens next lul
        // set the inPort boolean to true
        LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: PORT mode successfully enabled.");
        LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 200 PORT command successful");
        this.sendResponse("200 PORT command successful");
        this.inPort = true;
        return;
    }

    public void eprt(String address) {

        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "cmd eprt: Unable to enter extended port mode. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }

        // if eprt mode is not enabled, let the client know and return
        if (this.transferMode == 1) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "EPRT cmd: EPRT not available with this config.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 502 Command not implemented on this configuration.");
            this.sendResponse("502 Command not implemented on this configuration.");
            return;
        }

        // if its already in passive mode, turn off
        if(this.inPassive || this.inExtendedPassive || this.inPort) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Client currently connected for Passive/EPSV/PORT data transfers. Turning off other modes.");
            this.inPassive = false;
            this.inExtendedPassive = false;
            this.inPort = false;
            LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Passive/EPSV/PORT mode turned off. Ready to enter Extended Port mode.");
        }

        // parse eprt and send command
        // add eprt as an option to list, stor, retr
        LOGGER.log("[ON: " + this.thread_id + "] " + "EPRT cmd: Attempting to parse client cmd.");
        this.parseClientExtendedPort(address);
        LOGGER.log("[ON: " + this.thread_id + "] " + "EPRT cmd: Got port and host. Set variables.");
        LOGGER.log("Sent: 200 EPRT command successful.");
        this.sendResponse("200 EPRT command successful.");
        LOGGER.log("[ON: " + this.thread_id + "] " + "In Extended PORT mode.");
        this.inExtendedPort = true;
        return;
    }

    public void retr(String file) {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to retrieve files. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }

        // open listed file
        // read line by line and copy the file to the client host
        // do error checking, check to see if file exists
        // close connections
        if(this.inPassive || this.inExtendedPassive) {
            try {
                // in passive mode
                LOGGER.log("[ON: " + this.thread_id + "] " + "createConnection (PASV): opening data port socket.");
                ServerSocket ps = new ServerSocket(this.dataPort);
                Socket cds = ps.accept();
                this.dataWriter = new BufferedWriter(new OutputStreamWriter(cds.getOutputStream()));
                // open file
                File f = new File(file.substring(5));
                LOGGER.log("[ON: " + this.thread_id + "] " + "RETR cmd: Preparing to transfer file.");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 150 Opening ASCII mode data connection for " + f.getName());
                this.sendResponse("150 Opening ASCII mode data connection for " + f.getName());
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                    while ((line = br.readLine()) != null) {
                        // send line to client
                        this.sendData(line);
                    }
                } catch (IOException e) {
                    LOGGER.log("[ON: " + this.thread_id + "] " + "No longer able to read from file " + f.getName() + ".");
                    ps.close();
                    return;
                }
                cds.close();
                ps.close();
            } catch (IOException e) {
                LOGGER.log("LIST cmd: No acceptable connection from client to connect to.");
                return;
            }
        } else if (this.inPort || this.inExtendedPort) {
            try {
                // in port mode
                LOGGER.log("[ON: " + this.thread_id + "] " + "createConnection (PORT): opening data port socket.");
                Socket ps = new Socket(this.portHost, this.portDataPort);
                this.dataWriter = new BufferedWriter(new OutputStreamWriter(ps.getOutputStream()));
                // open file
                File f = new File(file.substring(5));
                LOGGER.log("[ON: " + this.thread_id + "] " + "RETR cmd: Preparing to transfer file.");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 150 Opening ASCII mode data connection for " + f.getName());
                this.sendResponse("150 Opening ASCII mode data connection for " + f.getName());
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                    while ((line = br.readLine()) != null) {
                        this.sendData(line);
                    }
                } catch (IOException e) {
                    LOGGER.log("[ON: " + this.thread_id + "] " + "No longer able to read from file " + f.getName() + ".");
                    ps.close();
                    return;
                }
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // neither in port or passive mode
            LOGGER.log("[ON: " + this.thread_id + "] " + "Neither port or passive modes are enabled.");
            return;
        }
        LOGGER.log("[ON: " + this.thread_id + "] " + "RETR cmd: File writing completed.");
        LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 226 Transfer complete");
        this.sendResponse("226 Transfer complete");
        return;
    }

    public void stor(String filename) {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to store files. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 532 Need account for storing files.");
            this.sendResponse("532 Need account for storing files.");
            return;
        }

        // create new file in current directory
        // open that file for writing
        // read from incoming client connection
        // write to file
        // close connections
        File f = new File(this.currDir + "/" + filename);
        LOGGER.log("File created: " + filename);
        if(this.inPassive || this.inExtendedPassive) {
            try {
                // in passive mode
                LOGGER.log("[ON: " + this.thread_id + "] " + "createConnection (PASV): opening data port socket.");
                ServerSocket ps = new ServerSocket(this.dataPort);
                Socket cds = ps.accept();
                BufferedInputStream fromClient = new BufferedInputStream(cds.getInputStream());
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
                LOGGER.log("[ON: " + this.thread_id + "] " + "STOR cmd: Preparing to read file contents from client.");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 150 Opening ASCII mode data connection for " + f.getName());
                this.sendResponse("150 Opening ASCII mode data connection for " + f.getName());
                byte[] bufSize = new byte[4096];
				int bytesRead;
				while((bytesRead = fromClient.read(bufSize)) != -1) {
					LOGGER.log("Bytes read from client: " + bytesRead);
					out.write(bufSize, 0, bytesRead);
				}
				out.flush();
                out.close();
                fromClient.close();
                cds.close();
                ps.close();
            } catch (IOException e) {
                LOGGER.log("LIST cmd: No acceptable connection from client to connect to.");
                return;
            }
        } else if (this.inPort || this.inExtendedPort) {
            try {
                // in port mode
                LOGGER.log("[ON: " + this.thread_id + "] " + "createConnection (PORT): opening data port socket.");
                Socket ps = new Socket(this.portHost, this.portDataPort);
                BufferedInputStream fromClient = new BufferedInputStream(ps.getInputStream());
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
                LOGGER.log("[ON: " + this.thread_id + "] " + "STOR cmd: Preparing to read file contents from client.");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 150 Opening ASCII mode data connection for " + f.getName());
                this.sendResponse("150 Opening ASCII mode data connection for " + f.getName());
                byte[] bufSize = new byte[4096];
				int bytesRead;
				while((bytesRead = fromClient.read(bufSize)) != -1) {
					LOGGER.log("Bytes read from client: " + bytesRead);
					out.write(bufSize, 0, bytesRead);
				}
				out.flush();
                out.close();
                fromClient.close();
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // neither in port or passive mode
            LOGGER.log("[ON: " + this.thread_id + "] " + "Neither port or passive modes are enabled.");
            return;
        }
        LOGGER.log("[ON: " + this.thread_id + "] " + "STOR cmd: File read/writing completed.");
        LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 226 Transfer complete");
        this.sendResponse("226 Transfer complete");
        return;
    }

    // Prints the current directory
    public void pwd() {
        LOGGER.log("[ON: " + this.thread_id + "] " + "Getting current system directory.");
        LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 257 \\" + this.currDir + "\\ is the current working directory");
        this.sendResponse("257 \\" + this.currDir + "\\ is the current working directory");
        return;
    }

    public void syst() {
        LOGGER.log("[ON: " + this.thread_id + "] " + "SYST cmd: Printing system information.");
        String info = System.getProperty("os.name");
        this.sendResponse("215 " + info);
        return;
    }

    public void list(String d) {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to send directory listing. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }

        File directory;
        if (d.equals("")) {
            directory = new File(this.currDir);
        } else {
            directory = new File(this.currDir + "/" + d);
        }

        if(this.inPassive || this.inExtendedPassive) {
            try {
                LOGGER.log("[ON: " + this.thread_id + "] " + "createConnection (PASV): opening data port socket.");
                ServerSocket ps = new ServerSocket(this.dataPort);
                Socket cds = ps.accept();
                this.dataWriter = new BufferedWriter(new OutputStreamWriter(cds.getOutputStream()));
                LOGGER.log("[ON: " + this.thread_id + "] " + "LIST cmd: Attempting to write data");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 150 Here comes the directory listing.");
                this.sendResponse("150 Here comes the directory listing.");
                File[] filesList = directory.listFiles();
                ArrayList<String> files = new ArrayList<>();
                ArrayList<String> directories = new ArrayList<>();
                for(File f : filesList){
                    if(f.isDirectory())
                        directories.add("-------Directory------ " + f.getName());
                    else if(f.isFile()){
                        files.add("---------File-------- " + f.getName());
                    }
                }
                ArrayList<String> listings = new ArrayList<>(directories);
                listings.addAll(files);
                for (int i = 0; i < listings.size(); i++) {
                    this.sendData(listings.get(i));
                }
                cds.close();
                ps.close();
            } catch (IOException e) {
                LOGGER.log("LIST cmd: No acceptable connection from client to connect to.");
                return;
            }
        } else if (this.inPort || this.inExtendedPort) {
            try {
                // in port mode
                LOGGER.log("[ON: " + this.thread_id + "] " + "createConnection (PORT): opening data port socket.");
                Socket ps = new Socket(this.portHost, this.portDataPort);
                this.dataWriter = new BufferedWriter(new OutputStreamWriter(ps.getOutputStream()));
                LOGGER.log("[ON: " + this.thread_id + "] " + "LIST cmd: Attempting to write data");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 150 Here comes the directory listing.");
                this.sendResponse("150 Here comes the directory listing.");
                File[] filesList = directory.listFiles();
                ArrayList<String> files = new ArrayList<>();
                ArrayList<String> directories = new ArrayList<>();
                for(File f : filesList){
                    if(f.isDirectory())
                        directories.add("-------Directory------ " + f.getName());
                    else if(f.isFile()){
                        files.add("---------File-------- " + f.getName());
                    }
                }
                ArrayList<String> listings = new ArrayList<>(directories);
                listings.addAll(files);
                for (int i = 0; i < listings.size(); i++) {
                    this.sendData(listings.get(i));
                }
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // neither in port or passive mode
            LOGGER.log("[ON: " + this.thread_id + "] " + "Neither port or passive modes are enabled.");
            return;
        }
        LOGGER.log("[ON: " + this.thread_id + "] " + "Data writing completed.");
        LOGGER.log("[ON: " + this.thread_id + "] " +"Sent: 226 Directory send OK.");
        this.sendResponse("226 Directory send OK.");
        return;
    }

    // print available commands
    public void help() {
        // display all available commands to client
        LOGGER.log("HELP cmd: Sending help commands to client.");
        // Does this even make sense to have? Most FTP clients have a help command
        // on their end. Requires no communication from the server.
        return;
    }

    public void parseCmds(String cmd) {
        String[] args;
        try {
            args = cmd.split(" ");
        } catch (NullPointerException e) {
            LOGGER.log("Client closed the connection.");
            this.quit();
            return;
        }
        LOGGER.log("[ON: " + this.thread_id + "] " + "cmd Received from Client: "+ args[0]);
        switch(args[0].toUpperCase()) {
            case "USER":
                this.user(args[1]);
                break;
            case "PASS":
                this.pass(args[1]);
                break;
            case "CWD":
                try {
                    this.cwd(args[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOGGER.log("[ON: " + this.thread_id + "] " + "CWD cmd: Not a valid argument.");
                    LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 500 Not a valid argument for CWD command.");
                    this.sendResponse("500 Not a valid argument for CWD command.");
                }
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
                try {
                    this.port(args[1]);
                } catch (IllegalArgumentException e) {
                    LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Not a valid argument.");
                    LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 500 Not a valid argument for PORT command.");
                    this.sendResponse("500 Not a valid argument for PORT command.");
                }
                break;
            case "EPRT":
                try {
                    this.eprt(args[1]);
                } catch (IllegalArgumentException e) {
                    LOGGER.log("[ON: " + this.thread_id + "] " + "EPRT cmd: Not a valid argument.");
                    LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 500 Not a valid argument for EPRT command.");
                    this.sendResponse("500 Not a valid argument for EPRT command.");
                }
                break;
            case "RETR":
                try {
                    this.retr(args[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOGGER.log("[ON: " + this.thread_id + "] " + "No file provided for RETR cmd.");
                    LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 500 Not a valid argument for RETR command.");
                    this.sendResponse("500 Not a valid argument for RETR command.");
                    break;
                }
                break;
            case "STOR":
            try {
                this.stor(args[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOGGER.log("[ON: " + this.thread_id + "] " + "No file provided for STOR cmd.");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 500 Not a valid argument for STOR command.");
                this.sendResponse("500 Not a valid argument for STOR command.");
                break;
            }
            break;
            case "PWD":
                this.pwd();
                break;
            case "SYST":
                this.syst();
                break;
            case "LIST":
                if (args.length == 1) {
                    this.list("");
                } else if (args.length == 2) {
                    this.list(args[1]);
                } else {
                    LOGGER.log("[ON: " + this.thread_id + "] " + "LIST cmd: Not a valid argument.");
                    LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: ");
                    this.sendResponse("500 ");
                }
                break;
            case "HELP":
                this.help();
                break;
            default:
                LOGGER.log("[ON: " + this.thread_id + "] " + "cmd: " + args[0] + " not recognized. Please try again.");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 502 Command not implemented");
                this.sendResponse("502 Command not implemented.");
                break;
        }
    }

    private void parseClientExtendedPort(String s) {
        String[] numbers = s.split("|");
        this.portHost = numbers[1];
        try {
            this.portDataPort = Integer.parseInt(numbers[2]);
        } catch (NumberFormatException e) {
            LOGGER.log("Could not convert String " + numbers[2] + " to an int.");
            return;
        } catch (ArrayIndexOutOfBoundsException x) {
            LOGGER.log("2nd index of Array not available Please check parsing methods.");
            return;
        }
        return;
    }

    // parse port commands from client
	private void parseClientPort(String s) {
		String[] numbers = s.split(",");
	    this.portHost = numbers[0] + "." + numbers [1] +"." + numbers[2] + "." + numbers [3];
		this.portDataPort = getPort(numbers[4], numbers[5]);
		return;
    }

    private int getEpsvPort() {
        List<Integer> ports = Arrays.asList(26503, 29319, 47495, 25223);
        ArrayList<Integer> avail_ports = new ArrayList<>();
        avail_ports.addAll(ports);
        Random num = new Random();
        int port = avail_ports.get(num.nextInt(avail_ports.size()));
        return port;
    }

    private ArrayList<Integer> getPasvPort() {
        List<Integer> ports = Arrays.asList(26503, 29319, 47495, 25223);
        ArrayList<Integer> avail_ports = new ArrayList<>();
        avail_ports.addAll(ports);
        Random num = new Random();
        Integer port = avail_ports.get(num.nextInt(avail_ports.size()));
        ArrayList<Integer> parsed_port = new ArrayList<>();
        Integer part_1 = 135;
        int temp_port = port -135;
        Integer part_2 =  temp_port / 256;
        parsed_port.add(part_2);
        parsed_port.add(part_1);
        parsed_port.add(port);
        return parsed_port;
        
    }

    // returns client socket port number
	private static int getPort(String num1, String num2) {
		int toNum1 = Integer.parseInt(num1);
		int toNum2 = Integer.parseInt(num2);
		int port = (toNum1 * 256) + toNum2;
		return port;
	}
}
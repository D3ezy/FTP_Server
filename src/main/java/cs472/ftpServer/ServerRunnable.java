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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

public class ServerRunnable extends Thread {

    private Logger LOGGER;
    private String currDir;
    private boolean userLoggedIn;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ServerSocket dataSocket;
    private Socket portSocket;
    private String portHost;
    private int dataPort;
    private int portDataPort;
    protected Socket clientSocket;
    private String username;

    ServerRunnable(Socket clientSocket, DataInputStream is, DataOutputStream os, Logger serverLog) {
        this.LOGGER = serverLog;
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(is));
        this.writer = new BufferedWriter(new OutputStreamWriter(os));
        this.userLoggedIn = false;
        this.currDir = System.getProperty("user.dir");
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
        this.sendResponse("331 Please specify the password.");
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
                    this.sendResponse("230 Login successful.");
                    br.close();
                    return;
                }
            }
            LOGGER.log("530 Login incorrect.");
            this.sendResponse("530 Login incorrect.");
            br.close();
        } catch (FileNotFoundException e) {
            LOGGER.log("Unable to find authenticated users file.");
        } catch (IOException x) {
        }
        return;
    }

    public void cwd(String directory) {
        if(!this.userLoggedIn) {
            LOGGER.log("Unable to change working directory. User not logged in.");
            LOGGER.log("Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        return;
    }

    public void cdup() {
        if(!this.userLoggedIn) {
            LOGGER.log("Unable to cdup. User not logged in.");
            LOGGER.log("Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        return;
    }

    public void quit() {
        return;
    }

    public void pasv() {
        if(!this.userLoggedIn) {
            LOGGER.log("cmd pasv: Unable to enter passive mode. User not logged in.");
            LOGGER.log("Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        return;
    }

    public void epsv() {
        if(!this.userLoggedIn) {
            LOGGER.log("cmd epsv: Unable to enter extended passive mode. User not logged in.");
            LOGGER.log("Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        return;
    }

    public void port(String address) {
        if(!this.userLoggedIn) {
            LOGGER.log("cmd port: Unable to enable port mode. User not logged in.");
            LOGGER.log("Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        // parse command to get host and port number
        this.parseClientPort(address);

        // establish connection with new socket to the client
        // return and wait to see what happens next lul
        return;
    }

    public void eprt(String address) {
        if(!this.userLoggedIn) {
            LOGGER.log("cmd eprt: Unable to enter extended port mode. User not logged in.");
            LOGGER.log("Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        return;
    }

    public void retr(String file) {
        if(!this.userLoggedIn) {
            LOGGER.log("Unable to retrieve files. User not logged in.");
            LOGGER.log("Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        return;
    }

    public void stor(String filename) {
        if(!this.userLoggedIn) {
            LOGGER.log("Unable to store files. User not logged in.");
            LOGGER.log("Sent: 532 Need account for storing files.");
            this.sendResponse("532 Need account for storing files.");
            return;
        }
        return;
    }

    // Prints the current directory
    public void pwd() {
        LOGGER.log("Getting current system directory.");
        LOGGER.log("Sent: 257 \"" + this.currDir + "\" is the current working directory");
        this.sendResponse("257 \"" + this.currDir + "\" is the current working directory");
        return;
    }

    public void syst() {
        LOGGER.log("SYST cmd: Printing system information.");
        String info = System.getProperty("os.name");
        this.sendResponse("215 " + info);
        return;
    }

    public void list(String d) {
        if(!this.userLoggedIn) {
            LOGGER.log("Unable to send directory listing. User not logged in.");
            LOGGER.log("Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        File directory;
        if (d.equals("")) {
            directory = new File(this.currDir);
        } else {
            directory = new File(this.currDir + d);
        }
        File[] filesList = directory.listFiles();
        for(File f : filesList){
            if(f.isDirectory())
                System.out.println(f.getName());
            if(f.isFile()){
                System.out.println(f.getName());
            }
        }
        return;
    }

    // print available commands
    public void help() {
        return;
    }

    public void parseCmds(String cmd) {
        String[] args = cmd.split(" ");
        LOGGER.log("cmd Received from Client: "+ args[0]);
        switch(args[0].toUpperCase()) {
            case "USER":
                this.user(args[1]);
                break;
            case "PASS":
                this.pass(args[1]);
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
                try {
                    this.port(args[1]);
                } catch (IllegalArgumentException e) {
                    LOGGER.log("PORT cmd: Not a valid argument.");
                    LOGGER.log("Sent: 500 Not a valid argument for PORT command.");
                    this.sendResponse("500 Not a valid argument for PORT command.");
                }
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
                if (args.length == 1) {
                    this.list("");
                } else if (args.length == 2) {
                    this.list(args[1]);
                } else {
                    LOGGER.log("LIST cmd: Not a valid argument.");
                    LOGGER.log("Sent: ");
                    this.sendResponse("500 ");
                }
                break;
            case "HELP":
                this.help();
                break;
            default:
                LOGGER.log("cmd: " + args[0] + " not recognized. Please try again.");
                LOGGER.log("Sent: 502 Command not implemented");
                this.sendResponse("502 Command not implemented.");
                break;
        }
    }

    // parse port commands from client
	private void parseClientPort(String s) {
		String[] numbers = s.split(",");
	    this.portHost = numbers[0] + "." + numbers [1] +"." + numbers[2] + "." + numbers [3];
		this.portDataPort = getPort(numbers[4], numbers[5]);
		return;
    }

    // returns client socket port number
	private static int getPort(String num1, String num2) {
		int toNum1 = Integer.parseInt(num1);
		int toNum2 = Integer.parseInt(num2);
		int port = (toNum1 * 256) + toNum2;
		return port;
	}
}
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

public class ServerRunnable extends Thread {

    private Logger LOGGER;
    private String currDir;
    private boolean userLoggedIn;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ServerSocket passiveSocket;
    private Socket portSocket;
    private String portHost;
    private int dataPort;
    private int portDataPort;
    protected Socket clientSocket;
    private String username;
    private String thread_id;
    private boolean inPassive;
    private boolean inPort;

    ServerRunnable(Socket clientSocket, DataInputStream is, DataOutputStream os, Logger serverLog) {
        this.LOGGER = serverLog;
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(is));
        this.writer = new BufferedWriter(new OutputStreamWriter(os));
        this.userLoggedIn = false;
        this.currDir = System.getProperty("user.dir");
        this.inPassive = false;
        this.inPort = false;
    }

    @Override
    public void run() {
        // print message
        this.sendResponse("220 Connected. DeezyFTP - Welcome!");
        this.thread_id = Thread.currentThread().getName();
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
        return;
    }

    public void cdup() {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to cdup. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        return;
    }

    public void quit() {
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

        if(this.inPort) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: Client currently connected for PORT data transfers. Turning off PORT mode.");
            this.inPort = false;
            LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: PORT mode turned off. Ready to enter Passive mode.");
        }
        // test, but get local IP here and store in pasv_cmd
        String pasv_cmd = "127,0,0,1";
        LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: Got IP Address. Added to string.");
        LOGGER.log("[ON: " + this.thread_id + "] " + "PASV cmd: Parsing random selected port. Adding to string.");
        // String pasv_cmd;
        // parse port and sent host/port to client
        int port = this.getPasvPort();
        int port_1, port_2;
        LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 227 Entering Passive Mode (" + pasv_cmd + ").");
        this.sendResponse("227 Entering Passive Mode (" + pasv_cmd + ").");

        // set the data connections to prepare for passive transfers
        this.dataPort = 1;
        this.inPassive = true;
        return;
    }

    public void epsv() {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "cmd epsv: Unable to enter extended passive mode. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        return;
    }

    public void port(String address) {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Unable to enable port mode. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }

        // if its already in passive mode, turn off
        if(this.inPassive) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Client currently connected for Passive data transfers. Turning off Passive mode.");
            this.inPassive = false;
            LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Passive mode turned off. Ready to enter Passive mode.");
        }

        // parse command to get host and port number
        this.parseClientPort(address);

        // return and wait to see what happens next lul
        // set the data port to the correct port
        // set the inPort boolean to true
        this.portDataPort = 1; // placeholder
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
        return;
    }

    public void retr(String file) {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to retrieve files. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 530 Not logged in.");
            this.sendResponse("530 Not logged in.");
            return;
        }
        return;
    }

    public void stor(String filename) {
        if(!this.userLoggedIn) {
            LOGGER.log("[ON: " + this.thread_id + "] " + "Unable to store files. User not logged in.");
            LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 532 Need account for storing files.");
            this.sendResponse("532 Need account for storing files.");
            return;
        }
        return;
    }

    // Prints the current directory
    public void pwd() {
        LOGGER.log("[ON: " + this.thread_id + "] " + "Getting current system directory.");
        LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 257 \"" + this.currDir + "\" is the current working directory");
        this.sendResponse("257 \"" + this.currDir + "\" is the current working directory");
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
        LOGGER.log("[ON: " + this.thread_id + "] " + "cmd Received from Client: "+ args[0]);
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
                    LOGGER.log("[ON: " + this.thread_id + "] " + "PORT cmd: Not a valid argument.");
                    LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 500 Not a valid argument for PORT command.");
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

    // initiate data connection
    private void createConnection() {
        if (this.inPassive) {
            try {
                LOGGER.log("[ON: " + this.thread_id + "] " + "createConnection (PASV): opening data port socket.");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 150 Opening ASCII mode data connection");
                this.sendResponse("150 Opening ASCII mode data connection");
                this.passiveSocket = new ServerSocket(this.dataPort);
            } catch (IOException e) {

            }
            return;
        } else if (this.inPort) {
            try {
                LOGGER.log("[ON: " + this.thread_id + "] " + "createConnection (PORT): opening data port socket.");
                LOGGER.log("[ON: " + this.thread_id + "] " + "Sent: 150 Opening ASCII mode data connection");
                this.sendResponse("150 Opening ASCII mode data connection");
                this.portSocket = new Socket(this.portHost, this.portDataPort);
            } catch (IOException e) {

            }
            return;
        } else {    
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

    private int getPasvPort() {
        List<Integer> ports = Arrays.asList(39392, 39451, 39567, 21143);
        ArrayList<Integer> avail_ports = new ArrayList<>();
        avail_ports.addAll(ports);

        Random num = new Random();
        return avail_ports.get(num.nextInt(avail_ports.size()));
        
    }

    // returns client socket port number
	private static int getPort(String num1, String num2) {
		int toNum1 = Integer.parseInt(num1);
		int toNum2 = Integer.parseInt(num2);
		int port = (toNum1 * 256) + toNum2;
		return port;
	}
}
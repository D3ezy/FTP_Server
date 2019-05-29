/*
 * Main.java
 * 
 * Author: Matthew Dey
 * Date Created: May 27th, 2019
 * Drexel University
 * CS 472 - HW3 - Computer Networks
 * 
 */

package cs472.ftpServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class Main {

    private static Logger LOGGER = new Logger("./main.log");

    public static void main(String[] args) {
        // arg check
		if (args.length != 2) {
			LOGGER.log("Usage: java -jar .\\build\\libs\\CS472-ftpServer-all-x.x.jar <log file> <listening port> <config file>");
			System.exit(1);
		} 

        int mode = parseConfig(args[3]);
        Server s = new Server(args[0], args[1], mode);
        s.run();
    }

    private static int parseConfig(String filename) {

        // read the file line-by-line
        boolean port_mode = false;
        boolean pasv_mode = false;
        FileReader fr;
        BufferedReader br;
        try {
            File f = new File(filename);
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String line;
            // ignore pound symbols
            // find config commands
            // str.split() by '=' sign
            // read args[0] for port/pasv cmd
            while((line = br.readLine()) != null) {
                char comm = line.charAt(0);
                if (comm == '#') {
                    LOGGER.log("PARSE_CFG: Found comment line. Skipping.");
                } else {
                    String[] cmd = line.split("=");
                    if (cmd[0].equalsIgnoreCase("PORT_MODE")) {
                        if (cmd[1].equalsIgnoreCase("ON")) {
                            LOGGER.log("PARSE_CFG: Found PORT config. Turning on PORT mode for server.");
                            port_mode = true;
                        }
                    } else if (cmd[0].equalsIgnoreCase("PASV_MODE")) {
                        if (cmd[1].equalsIgnoreCase("ON")) {
                            LOGGER.log("PARSE_CFG: Found PASV config. Turning on PASV mode for server.");
                            pasv_mode = true;
                        }
                    } else {
                        LOGGER.log("PARSE_CFG: Parsed line does not have any valid config commands. Moving to the next line.");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.log("PARSE_CFG: File not found, please run the program with a valid config file name.");
            LOGGER.log("PARSE_CFG: Config file usage: [NAME] ftpserverd.conf [ATTRIB] port/pasv_mode = ON/OFF");
            System.exit(1);
        } catch (IOException x) {
            LOGGER.log("PARSE_CFG: Unable to reader config file, please try again. Make sure the file is valid and readable.");
            System.exit(1);
        }

        // if both return 0
        // if pasv return 1
        // if port return 2
        // if lines cannot be read, return fatal error on instantiation
        if (port_mode && pasv_mode) {
            LOGGER.log("PARSE_CFG: Both PORT and PASV mode are enabled for data transfers. Reuturning 0.");
            return 0;
        } else if (port_mode) {
            LOGGER.log("PARSE_CFG: Only PORT is enabled for data transfers. Reuturning 2.");
            return 2;
        } else if (pasv_mode) {
            LOGGER.log("PARSE_CFG: Only PASV mode is enabled for data transfers. Reuturning 1.");
            return 1;
        } else {
            LOGGER.log("PARSE_CFG: FATAL ERROR: Neither PORT or PASV mode has been enabled in the config. Cannot run the server like this.");
            LOGGER.log("PARSE_CFG: Please enable a transfer mode. Config file usage: [NAME] ftpserverd.conf [ATTRIB] port/pasv_mode = ON/OFF");
            System.exit(3);
        }
        return 0;
    }
}


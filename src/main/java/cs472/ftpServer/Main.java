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

        Server s = new Server(args[0], args[1]);
        s.run();
    }

    private int parseConfig(String filename) {
        // 0 = both on
        // 1 = pasv
        // 2 = port
        // 3 = both off (Fatal)

        // read the file line-by-line
        FileReader fr;
        BufferedReader br;
        try {
            File f = new File(filename);
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String line;
            while((line = br.readLine()) != null) {
                char comm = line.charAt(0);
                if (comm == '#') {
                    LOGGER.log("PARSE_CFG: Found comment line. Skipping.");
            LOGGER.log("PARSE_CFG: File not found, please run the program with a valid config file name.");
            LOGGER.log("PARSE_CFG: Config file usage: [NAME] ftpserverd.conf [ATTRIB] port/pasv_mode = ON/OFF");
            System.exit(1);
        } catch (IOException x) {
            LOGGER.log("PARSE_CFG: Unable to reader config file, please try again. Make sure the file is valid and readable.");
            System.exit(1);
        }
        // ignore pound symbols
        // find config commands
        // str.split() by '=' sign
        // read args[0] for port/pasv cmd
        // if both return 0
        // if pasv return 1
        // if port return 2
        // if lines cannot be read, return fatal error on instantiation
        return 0;
    }
}


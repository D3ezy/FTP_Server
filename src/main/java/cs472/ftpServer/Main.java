/*
 * Main.java
 * 
 * Author: Matthew Dey
 * Date Created: May 3nd, 2019
 * Drexel University
 * CS 472 - HW3 - Computer Networks
 * 
 */

package cs472.ftpServer;

public class Main {

    private static Logger LOGGER = new Logger("./main.log");

    public static void main(String[] args) {
        // arg check
		if (args.length != 2) {
			LOGGER.log("Usage: java -jar .\\build\\libs\\CS472-ftpServer-all-x.x.jar <listening port> <log file>");
			System.exit(1);
		} 

		// Initialize a new client and show the menu
        Server s = new Server(args[0], args[1]);
        new Thread(s).start();
        s.run();
    }
}

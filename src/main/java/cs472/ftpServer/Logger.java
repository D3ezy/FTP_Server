/*
 * Logger.java
 * 
 * Author: Matthew Dey
 * Date Created: May 3nd, 2019
 * Drexel University
 * CS 472 - HW3 - Computer Networks
 * 
 */

package cs472.ftpServer;

import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime; 
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

    private String path;

    // Create a new logger with the logger path
    Logger(String filepath) {
        path = filepath;
    }

    // Log (with timestamp) the responses and input sent by the function call.
    public void log(String s) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
        LocalDateTime now = LocalDateTime.now();
        String currTime = dtf.format(now);
        File f = new File(this.path);
        System.out.println("[" + currTime + "]" + ": " + s);
        try {
            FileWriter fw = new FileWriter(f, true);
            PrintWriter out = new PrintWriter(fw);
            out.println("[" + currTime + "]" + ": " + s);
            out.close();
            fw.close();
        } catch (IOException e) {
            System.out.println("Logging.log: Unable to write to file.");
            return;
        }
    }
}
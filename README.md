# cs472-ftpServer
HW2 - FTP Server for CS 472 (Computer Networks) Drexel
Author: Matthew Dey
Date: 05-21-2019

TO RUN:

This build uses Gradle for dependency compiling. The Gradle build has a task to create a "fatJar" that can be run with the given parameters below in the root folder of the assignment:

java -jar .\build\libs\cs472-ftpServer-all-0.1.jar <log file> <ip addr>

Examples: 
java -jar .\build\libs\cs472-ftpServer-all-0.1.jar C:\Users\matth\Desktop\server.log localhost
java -jar .\build\libs\cs472-ftpClient-all-0.1.jar server.log 39391


Answers to HW Questions:
1. Consider the security of the data of FTP and the commands. Good or bad? Is BitTorrent better or worse? Why? What about SFTP? Is it the way that it is implemented or are there considerations because of how the protocol works? (Look up the basics of SFTP and BitTorrent) 

2. Do you think that someone could hack into your FTP server? Why or why not? Through what methods? 

3. EXTRA CREDIT: Critique how FTP solves the problem of file transfer – what is good? What is bad? What is weird? 

4. EXTRA CREDIT: How hard would it be to make FTP use one channel instead of two?

Files:

References: 
RFC 959 - https://tools.ietf.org/html/rfc959
RFC 2428 - https://tools.ietf.org/html/rfc2428


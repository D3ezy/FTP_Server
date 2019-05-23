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

NOTE: Some GUI FTP Clients send extra commands on their own that are not implemented in this server. This server was tested working with my HW2 FTP Client.

Answers to HW Questions:
1. Consider the security of the data of FTP and the commands. Good or bad? Is BitTorrent better or worse? Why? What about SFTP? Is it the way that it is implemented or are there considerations because of how the protocol works? (Look up the basics of SFTP and BitTorrent) 

2. Do you think that someone could hack into your FTP server? Why or why not? Through what methods? 

I think that it is resonable that someone would be able to hack into my FTP Server. There are not too many security considerations that are taken with a non-secure protocol
like FTP. There are much better protocol versions of FTP such as FTPS or SFTP which are encrypted over the network. My FTP server has a few reasons as to why it could be breached. 
The main reason is the the usernames and passwords are sent as plain text to the server, so if there was a known communication between client and server, someone could sniff my network and see what usernames and passwords are being authenticated based on FTP return codes. 

3. EXTRA CREDIT: Critique how FTP solves the problem of file transfer – what is good? What is bad? What is weird? 

The way FTP solves file transfer, for the time that the protocol was created, was pretty innovative. It requires two ports to be open at most. One for transferring data, and the other for acknowledgement communication based on return codes from the server to the client. This is good because there are separate streams for data and communication, which streamlines processes and can benefit speed. However, there are some bad parts about file transfer. For one, it does NOT use any sort of acknowledgement that the client got all the bytes required of the file it was requesting. FTP just sends bytes as long as there are bytes to send and the connection for the data transfer is established. 

4. EXTRA CREDIT: How hard would it be to make FTP use one channel instead of two?

It would be pretty hard to make FTP use one channel instead of two because of the way Socket blocking works. Additionally, it would be difficult because it wouldn't be FTP anymore. The protocol is specifically designed with the mindset that data would be transferred using a second connection. New ACKs and NACKs would have to be introduced in order to keep client/server communication. This could be done by reimplementing a new version of the FTP protocol, with new ACKs and a sliding window setup. This would also not allow the PORT/PASV command to be used, because the client would not be allowed to act as a server nor would the server be able to send a new port to connect to. I think that this could be done, but at a cost and it would not be worth it.

Files:

References: 
RFC 959 - https://tools.ietf.org/html/rfc959
RFC 2428 - https://tools.ietf.org/html/rfc2428


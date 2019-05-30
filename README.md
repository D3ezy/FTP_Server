# cs472-ftpServer
HW2 - FTP Server for CS 472 (Computer Networks) Drexel
Author: Matthew Dey
Date: 05-21-2019
Update for HW4: 05-27-2019

NOTE: HW4 Answers are at the bottom of the page. This was used on a different branch of the same repository.

TO RUN:

This build uses Gradle for dependency compiling. The Gradle build has a task to create a "fatJar" that can be run with the given parameters below in the root folder of the assignment:

java -jar .\build\libs\cs472-ftpServer-all-0.1.jar <log file> <ip addr>

Examples: 
java -jar .\build\libs\cs472-ftpServer-all-0.1.jar C:\Users\matth\Desktop\server.log localhost
java -jar .\build\libs\cs472-ftpClient-all-0.1.jar server.log 39391

NOTE: Some GUI FTP Clients send extra commands on their own that are not implemented in this server. This server was tested working with my HW2 FTP Client.

NOTE: Config file is listed as "ftpserverd.conf" in the root directory. The "main.log" is the log for parsing the commands. I did not feel this belonged in the Server log as this is something that happens before the server is spun up, as most application do and have multiple log files for different operations. You can separate the whitespace and the captalization and the configurations for PORT and PASV will still be parsed and read accordingly. Hashtags denote comments.

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

Answers to HW4 Questions:

1. What are the security considerations with port_mode? With pasv_mode? Why would I
want one or the other (think about some of the problems that you had with the client and
the server – and who calls who)? Think of the conversation between client and server. 

Logging

2. Why is logging an important part of security?
3. Do you see any problems with concurrent servers and log files? (dealing with multiple
processes or threads writing to the log file at the same time)? Brainstorm how to solve
this problem. 

Securing the connection with SSL/TLS 

4. What are the different issues with securing the connections with IMPLICIT mode (which
is a separate server listening with TLS always at port 990 by default) and EXPLICIT
mode (which is the same server waiting on port 21 for special commands to turn TLS on
and off)? What are the “it depends” part of the tradeoffs?

Analyzing the conversation

5. Why is the 3 person method of FTP (as originally defined in the RFC) really insecure?
Think of what you could do to cause trouble with the approach and what you can do in
your clients and servers to stop that from happening. Do you have to do any checking in
your program(s) with PORT/PASV to make sure that it isn’t happening (that YOU ARE
talking to the same host)? Think about the data channel as well as the control channel.

EXTRA CREDIT (worth up to 10 points): Think of the conversation of FTP and compare it to
other file transfer protocols

 SFTP – offers the service on port 22 and data and commands share the same channel
– better or worse?
 BitTorrent – offers files from a large number of hosts.

What are the good points and bad points of each approach (FTP, SFTP, BitTorrent)?

Analyzing the Operation of the Server

6. Do you think there are events that you’ve logged which show that someone is trying to
break into your server? Do you have to add other log entries and checking to make sure
that attacks aren’t happening? Brainstorm some attacks against your FTP server. For at
least one of them, implement additional code to check for them and log appropriate
entries. 


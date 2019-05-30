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

The security considerations differ between PORT mode and PASV mode using the FTP protocol. The main difference is how they are instantiated and used for data transfers. Both of these methods
of data transfer require a separate connection to be open between the client and the server to transfer the data, using the original port for communication. With PASV, or passive mode, the client sends the command to the server and the server responds with a connection host IP and a port to connect to. This has the client initiate another connection to the server on a separate port. With PORT, or port mode, the client acts as a server and send the FTP server the hosting IP address and a port to connect to. The server then initiates a connect with the client. Hence, the client acting as its own server for data transfers. The conversations and the security considerations from each of these modes differ. The security consideration for passive mode are that you want to be able to verify that the client you sent the command and port to connect to you on the data port, is the client that actually connects to the data port. With port mode, you want to make sure that the IP and Port you send in the command to the server actually gets to the server by sending the response code. You also want to make sure that the person you're opening up a data port for on the client is actually the server you sent the command. In either scernario, these IP's and Ports can be easily sniffed through the network because they are plain text. It wouldn't require much work for a hacker to get the IP and port and pretend to be the client/server and connect. You would prefer the passive FTP if you cannot configure the clients firewall to open PORTs for Active FTP. The problem is that neither of these are truly secure. PORT is a tad more secure that passive because the client has control of its firewall, but it is still not sent over SSL so you can see everything while sniffing the network. 

Logging

2. Why is logging an important part of security?

Logging is an important part of security because a good log will be able to tell the developer or engineer that is looking at it exactly what events were taking place at exactly which time. In the case of our FTP Server, the log would be able to show exactly what was going on in the protocol and the state of the server at a precise time. This will allow the viewer to see what commands were excuted, was code and replies were sent back to the client, and the networking information between the two. The log will be able to show what a server and a client are doing on the backend as a result of the communications between the two. Logging is mainly important because it lays out exact dates and times of everything going on in the server that is logged, so it is easy to retrace our steps and give us a "play-by-play" of what is happening on the server. This is important because if someone hacks into the server, we can see what they did and how it went wrong for the server.

3. Do you see any problems with concurrent servers and log files? (dealing with multiple
processes or threads writing to the log file at the same time)? Brainstorm how to solve
this problem. 

I do see a few problems (and did while coding) regarding a concurrent server and logging. There are a few solutions that I was able to bring to the table and decide on when programming my server. The problem with a single log file and a concurrent server is that multiple clients can be sending requests to the server and the server has to write to the log at the same time. This can cause errors, exception, and it can even throw off the timestamps of when actions were excuted between a server and multiple clients. The errors may be caused if the same file is being written to at once. I had two ideas to fix this and ended up going with one in my programming. The first idea was to have a separate log for each thread. This would definitely fix the issue, but cause some calamity in how many logs there were. This is not a very viable solution for that reason. The second idea I brainstormed was to have the server threads right to the same log file, but add a block call if the file was already being written to. Additionally, I added the thread ID from Java so that I knew which thread the logs were coming from. Example is posted below: 

[2019/05/30 13:48:48]: [ON: Thread-0] Sent: 227 Entering Passive Mode (127,0,0,1,98,135).
[2019/05/30 13:48:48]: [ON: Thread-0] cmd Received from Client: list
[2019/05/30 13:48:48]: [ON: Thread-0] createConnection (PASV): opening data port socket.
[2019/05/30 13:48:49]: [ON: Thread-0] LIST cmd: Attempting to write data
[2019/05/30 13:48:49]: [ON: Thread-0] Sent: 150 Here comes the directory listing.
[2019/05/30 13:48:49]: [ON: Thread-0] Data writing completed.
[2019/05/30 13:48:49]: [ON: Thread-0] Sent: 226 Directory send OK.
[2019/05/30 13:48:54]: [ON: Thread-1] cmd Received from Client: pwd
[2019/05/30 13:48:54]: [ON: Thread-1] Getting current system directory.
[2019/05/30 13:48:54]: [ON: Thread-1] Sent: 257 \C:\Users\mdey\Documents\CS472\cs472-ftpServer\ is the current working directory
[2019/05/30 13:48:57]: [ON: Thread-2] cmd Received from Client: pwd
[2019/05/30 13:48:57]: [ON: Thread-2] Getting current system directory.
[2019/05/30 13:48:57]: [ON: Thread-2] Sent: 257 \C:\Users\mdey\Documents\CS472\cs472-ftpServer\ is the current working directory


Securing the connection with SSL/TLS 

4. What are the different issues with securing the connections with IMPLICIT mode (which
is a separate server listening with TLS always at port 990 by default) and EXPLICIT
mode (which is the same server waiting on port 21 for special commands to turn TLS on
and off)? What are the “it depends” part of the tradeoffs?

Securing the connections in two different fashions IMPLICIT and EXPLICIT have separate implication and can have different tradeoffs. FTPS uses SSL to secure the FTP server while reaping the benefits of what makes an FTP server such a good protocol to use for transferring files. There are two ways to enable TLS over FTP. The first is IMPLICIT mode which has for the most part been deprecated except on legacy systems. IMPLICIT FTPS establishs an immediate encrypted connect via Port 990 on the FTP server before any communication can begin. It is always listening on that port. If the client connecting fails to create a secure connection on this port, it fails and the client cannot even login. This is more strict than EXPLICIT FTPS but the protocol is not the standard. EXPLICIT FTPS establishes a connection like traditional FTP on Port 21, but before any authentication or data transfers can begin, the client first must authenticate and a TLS/SSL connection must be established. This is what is used in the FTPS protocol widely today, and it is the newest. It is regarded as the most secure way to use FTP. While EXPLICIT FTPS is less strict than IMPLICIT, it is regarded as "good enough" and it depends on what the user requires of the FTPS connection. It requires more work to get IMPLICIT FTPS working. If you are accessing and transferring files outside your local network IMPLICIT might be better because it is more strict, but if you're just using an internal FTP site, it may be easier/more beneficial to use EXPLICIT FTPS.

Analyzing the conversation

5. Why is the 3 person method of FTP (as originally defined in the RFC) really insecure?
Think of what you could do to cause trouble with the approach and what you can do in
your clients and servers to stop that from happening. Do you have to do any checking in
your program(s) with PORT/PASV to make sure that it isn’t happening (that YOU ARE
talking to the same host)? Think about the data channel as well as the control channel.

The 3 person method of FTP is really insecure for a few reasons. 

EXTRA CREDIT (worth up to 10 points): Think of the conversation of FTP and compare it to
other file transfer protocols

 SFTP – offers the service on port 22 and data and commands share the same channel
– better or worse?

This is better in a few aspects and worse in a few aspects. For one, SFTP is more secure. This is for two reasons. For one, you are not opening a separate port for data. The less ports open that hackers can sniff, the better. The next reason why it is more secure is because SFTP uses SSH. It runs over a one-channel encrypted communications of port 22 and this is for good reason. Passwords are not sent plain text. Instead SSH authentication keys are used to verify the identity of the user. For security reasons, this is very beneficial. However, this can become more complicated now, because things need to be organized unlike FTP. You're now dealing with a communication and data transfer layer on top of eachother over the same port. This requires ACKs and NACKs to make sure that the codes can be sent and a file is done transferring. A data connection cannot just be closed when the files bytes are all read, because the connection needs to stay open for communication. FTP is better in this sense, but it is also not secure.

 BitTorrent – offers files from a large number of hosts.

BitTorrent gathers artifacts of a download via trackers, and collects them all from different clients who also act as servers. This is beneficial because the more clients contributing to the download, the faster the download will be and the faster the trackers can find the portions of the download that they need. BitTorrent is secure in the sense that it allows multiple connections for downloads, but it is insecure in the sense that it cannot autheticate who is sending files. One drawback of BitTorrent is that the clients also have to act as servers, there is no central repository that hosts everything for download. This can cause download and performance issues on the client machines. FTP does not have this issue, it supports a large file download, serving the files to as many clients as it can handle. The issue is that it is centralized and it is not secure. So it is very easy for an intruder to hacker the server. 

What are the good points and bad points of each approach (FTP, SFTP, BitTorrent)?

FTP is good for large transfers on a local network. This is because it can handle large files in a separate data connection. It is not good, however, for connections outside of an internal network. This is because the traffic can easily be seen and usernames/passwords are sent in plain text. It is also very easy to read all the commands being sent to client and server. You can even see what data connections are being opened and intercept that. BitTorrent is also good for a local network in this sense. The protocol is easy to setup, and it uses packets from mutliple machines to piece together a download. This makes it very fast as well as hard to detect. Using trackers to find pieces of a download on a small internal network is very beneficial. However, it is hard to verify that the data you are getting is correct. Additionally with BitTorrent, if no one is "Seeding" the file, you cannot download it. With SFTP and FTP, you use a central server that is always serving files. Since the client is also a server, it drops performance drastically. SFTP is a more secure version of FTP in that it doesn't use two data ports. Instead it connects through Secure-Shell Host (SSH) and it uses authentication keys to verify the user is who they say they are. This is eons more secure than FTP is, and it can be used on an external network. SFTP becomes NAT and Firewall friendly because it only requires one port to be open. The drawbacks of SFTP are that it's slower, because the data is over an encrypted channel. 

Analyzing the Operation of the Server

6. Do you think there are events that you’ve logged which show that someone is trying to
break into your server? Do you have to add other log entries and checking to make sure
that attacks aren’t happening? Brainstorm some attacks against your FTP server. For at
least one of them, implement additional code to check for them and log appropriate
entries. 

I don't think that there are events that I've logged in my server which show that someone is trying to break in. This is because I tested locally on my machine or on an internal network where no one was sniffing traffic. However, it could be done. If I had tested from machine to machine outside my network, I think I would've been suseptable to hackers. I would definitely need to add other log entries to see if someone else had connected to my server that may not have been authorized. There are a few attacks that I could think of that may happen in any given situation. The first, is that someone could sniff my network traffic for large file transfers. Or even, just sniffing my network traffic they would see FTP protocol commands and exactly what they say. All that would need to be done was that user could wait for an authentication frame to roll by on Wireshark and take the username and password being authenticated and connect. Another type of attack that may happen (and this is more likely) is that a hacker might be able to find a file on a client machine or on a server machine and redirect that to his own machine. By sending the server or receiving the result of a PORT or PASV command, the hacker could spoof that address, and the FTP server has no way of knowing if it is the right machine, it just knows where to connect. So in theory, a hacker could receive the files that a client might actually be trying to receive, and have the server have no idea. Additions of code made to fix this could be adding a variable to check if a user is logged in already. If a user is already logged in, they can only be logged into one client. This would alleviate a hacker trying to authenticate while a user is logged in. The other fix required alterations to the protocol. I added an authentication method to the data port being opened briefly. This would force a user to authenticate at the data port as well to verify their integrity. I also added logging events that printed the IP with each event to see if that IP changed. If the IP changed, it would be easier to detect an intruder. 


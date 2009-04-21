This is the multi-user whiteboard created by Bob Yu, Michael Diaz-Tello, and Peter Tran.

To run the application, simply start the central server with 
"java Whiteboard.CentralServer"  The clients can then be invoked with 
"java Whiteboard.ClientGUI USERNAME" where username is the desired username
that is not already in use in the chatroom/whiteboard session. After 
invoking the client program, it will prompt the user for the IP address of
the Central Server (it will connect using the pre-agreed-upon port 3345) and
the program will then proceed to talk to the central server being hosted at
the IP supplied by the user.

To use the program, simply type text in the lower text field or select a 
drawing tool and begin drawing. After a line has been drawn, it will
immediately be forwarded to all users of the whiteboard system.

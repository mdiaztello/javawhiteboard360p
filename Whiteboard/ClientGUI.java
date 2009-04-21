/*------------------------ClientGUI-----------------------
 *
 * 
 * 	Author: Michael Diaz-Tello
 * 		with code derived from Ramesh Yerraballi's tcp and udp example code
 *
 * 	This class is the graphical front end for the client program. It takes care of 
 * 	establishing the connections and doing all of the graphical work so that the
 * 	user has a place to type their messages and display the old messages.
 *
 *
 * 	NOTE: for some strange reason, when i receive messages, the text area won't scroll down
 * 	but when i send messages it does.
 *
 *--------------------------------------------------------------*/



package Whiteboard;
import java.awt.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

//***********************************************
//Class: 		ClientGUI
//Description: 	contains the gui for the client
public class ClientGUI extends JFrame
{
	private	static JTextArea chatTextArea = new JTextArea(200, 150); // holds all the mssages from the chat window
	private static JTextField userTextField = new JTextField(30);    // input field for the user's messages
	private static Socket socket;
	private static ObjectOutputStream output;
	private static ObjectInputStream input;

	private static Client client;
	private static ArrayList<Client> clientList = null; 
	private static DatagramSocket udpSocket;
	private String serverip;
	private int serverport;

	// ***********************************************
	// constructor: ClientGUI
	// arguments: 	none
	// description: creates the chat window gui
	public ClientGUI(String clientName) throws IOException
	{
		setTitle("EE360P Chat Client - " + clientName);
		setLocationRelativeTo(null); //makes the window spawn in the middle of the screen
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		chatTextArea.setEditable(false); 		//the user can't edit the messages from other people chatting
		chatTextArea.setLineWrap(true); 		//word wrapping for big messages
		chatTextArea.setWrapStyleWord(true); 	//wrap on word boundaries

		userTextField.addActionListener( new SendMessageButtonListener()); 	//everytime the user hits enter, it will
																			//trigger the send message event

		JScrollPane scrollPane = new JScrollPane(chatTextArea); 						//new area where messages will appear
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 	//turn on the scrollbar
		scrollPane.setPreferredSize(new Dimension(450,110)); 							//sets how big the text area should be by default

		JButton disconnectMessageButton = new JButton("Disconnect from chat");
		disconnectMessageButton.addActionListener(new DisconnectButtonListener());

		
		JPanel contentPane = new JPanel(); //a JPanel holds everything and is contained by a JFrame (a blank window)
		contentPane.setLayout(new BorderLayout()); 

		contentPane.add(disconnectMessageButton, BorderLayout.NORTH);  	//place the disconnect button at the top
		contentPane.add(scrollPane, BorderLayout.CENTER);   			//the chat messages in the middle
		contentPane.add(userTextField, BorderLayout.SOUTH);				//and the user input area at the bottom
	
		setContentPane(contentPane);
		
		pack(); //finishes initializing the contentPane

		udpSocket = new DatagramSocket();//create the udp socket that the client will use for communication to the relayers
		
		try {
	        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	        String str = "";
	        System.out.print("Connect to server IP: ");
            str = in.readLine();
            serverip = str;
            System.out.print("Port: ");
            str = in.readLine();
            serverport = Integer.parseInt(str);
	    } catch (IOException e) {}
		
		socket = new Socket(serverip, serverport); 					//create connection to the server
		output = new ObjectOutputStream (socket.getOutputStream()); //create a way to send data to the server
		input = new ObjectInputStream (socket.getInputStream()); 	//create a way to send data to the server
	}

	// ***********************************************
	// method: 		updateTextArea
	// arguments: 	message object
	// description: adds a new text message to the chat window
	public static void updateTextArea(Message m)
	{
		String chatMessages = chatTextArea.getText() + "\n" + m;	
		
		chatTextArea.setText(chatMessages); //update chat message area
		chatTextArea.setCaretPosition(chatTextArea.getText().length());//this makes the text area autoscroll to the last message added
		userTextField.setText("");//clear the user's message area for new input
		
	}

	//***********************************************
	//Class: 		DisconnectButtonListener
	//Description: 	inner class that handles the events generated by clicking on the disconnect button
	class DisconnectButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			try
			{
				client.disconnect(socket, output);
			}
			catch (Exception exception) { exception.printStackTrace(); }
			System.exit(0); //exit the program	
		}
	}
	
	// ***********************************************
	// method: 		getClient
	// arguments: 	none
	// description: returns client object
	public static Client getClient()
	{
		return client;
	}
	// ***********************************************
	// method: 		getClientList
	// arguments: 	none
	// description: returns client list
	public static ArrayList<Client> getClientList()
	{
		return clientList;
	}
	// ***********************************************
	// method: 		getUDPPort
	// arguments: 	none
	// description: returns client's udp port
	public static DatagramSocket getUDPPort()
	{
		return udpSocket;
	}

	//***********************************************
	//Class: 		DisconnectButtonListener
	//Description: 	inner class that handles the events generated by
	//				pressing enter in the user text area in order to send a message
	class SendMessageButtonListener implements ActionListener
	{
		// ***********************************************
		// method: 		actionPerformed
		// arguments: 	actionevent handler
		// description: updates the textarea in the client gui
		public void actionPerformed(ActionEvent e)
		{
			String userMessage = userTextField.getText();// get the user's message	
			updateTextArea(new Message(client.getName(), userMessage));	
	
			try
			{
				//client.sendMessageToServer(socket, output, userMessage);
				client.relayChangesToClients(client, new WhiteboardChanges( null, new Message( client.getName(), userMessage) ),
					       	clientList, udpSocket);
			}
			catch(Exception exception) { exception.printStackTrace(); }
		}
	}

	@SuppressWarnings("unchecked") //make the compiler quit complaining about the perfectly valid clientList = (ArrayList<Client>)... line
	public static void main( String [] args) throws NumberFormatException, UnknownHostException, IOException   
	{
		ClientGUI gui = new ClientGUI(args[0]);
		gui.setVisible(true);
		client = new Client(args[0]);	
		client.setUDPPort(udpSocket.getLocalPort());
		client.connectToServer(socket, output, input);

		try
		{
			clientList = (ArrayList<Client>) input.readObject();	
		}	
		catch(ClassNotFoundException cnfe) { cnfe.printStackTrace(); }	
		System.out.println("This is the clientList:");
		for( Client c: clientList)
		{
			System.out.println(c);
		}
		
		Frame f = new Frame("Whiteboard - " + args[0]);
		Transport t;
		boolean readOnly = false;
		t = new LocalTransport();
		
		Whiteboard myBoard = new Whiteboard(t, readOnly);
		myBoard.buildWhiteboard(f);
		f.pack();
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				System.exit(0);
			}
		});
		t.start();
		f.show();

		while(true)
		{
			Object obj = null;
			obj =  client.udpReceiveObject(udpSocket);

			if(obj.getClass().equals(WhiteboardChanges.class))//check the object type to see if it is what we expected
			{
				WhiteboardChanges wbchanges = (WhiteboardChanges) obj;

				if(wbchanges.getMessage() != null) //did we get a message? or just a list of whiteboard changes?
				{
					Message m = wbchanges.getMessage();
					if( m.myName.equals("CentralServer"))
					{
						if(m.chatMsg.equals("***disconnect client***"))
						{
							Client c = (Client) client.udpReceiveObject(udpSocket);
							clientList.remove(clientList.indexOf(c));
							System.out.println("\nThe new client list is now the following:\n");
							for( Client cl: clientList)
							{
								System.out.println(cl);
							}
							updateTextArea(new Message("CentralServer", c.getName() + " has left the chatroom."));
						}
						else if( m.chatMsg.equals("***client connected***"))
						{
							Client c = (Client) client.udpReceiveObject(udpSocket);
							clientList.add(c);
							System.out.println("\nThe new client list is now the following:\n");
							for( Client cl: clientList)
							{
								System.out.println(cl);
							}
							updateTextArea(new Message("CentralServer", c.getName() + " has joined the chatroom."));
						}
						else 
							System.out.println("SOMETHING WACKY HAPPENED!");
							
							
					}
					else if( m.myName.equals("WhiteboardChange"))
					{
						System.out.println(wbchanges.getChangeList().shapeToString());
						// code here
						myBoard.addShape(wbchanges.getChangeList());
						myBoard.setRepaint(true);
						myBoard.repaint();
					}
					else
						updateTextArea(m);
				}

			}
			else //if not, skip this iteration and try again
			{
				System.out.println("WE GOT A WEIRD OBJECT!");
				//System.out.println(obj);
				continue;
			}
		}
	}
}


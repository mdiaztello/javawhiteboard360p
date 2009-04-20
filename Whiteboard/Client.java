/*---------------------Client------------------------------
 *
 *
 * 	Author: Michael Diaz-Tello
 * 		with code derived from Ramesh Yerraballi's tcp and udp example code
 *
 * MY UDP CODE IS DERIVED FROM THE UPDCLIENT AND UDPSERVER EXAMPLES GIVEN
 * TO THE CLASS BY RAMESH YERRABALLI
 *
 * Description: this program represents the way for a user
 * to send messages to the chat room. It keeps track of the
 * user's name and its IP address. It sends all of its chat
 * messages to the relayers for relaying to other clients
 * and it makes a TCP connection to the central server to
 * determine which relayers are available to connect to.
 *
 * ORIGINALLY FROM RELAYER:
 *
 * This is a server that will relay messages sent by clients to other clients
 * that are a part of the chat room. Since there are multiple relayers, it will
 * relay messages to other relayers, who in turn relay the messages to their clients
 * ----------------------------------------------------------*/


package Whiteboard;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client implements Serializable
{

	//fields
	
	private String clientName;	
	private InetAddress myIP;
	private int udpPort;
	//private Message m; 
	
	//constructors
	
	public Client(String name)
	{
		setName(name);
		try
		{
			myIP = InetAddress.getLocalHost(); 	
		}
		catch(Exception e) { e.printStackTrace(); }
		
	}
	//methods
//accessors
	public InetAddress getIP()
	{
		return myIP;
	}
	public String getName()
	{
		return clientName;
	}	
	
	public int getUDPPort()
	{
		return udpPort;
	}
//modifiers
	public void setName(String newName)
	{
		clientName = newName;
	}
	
	public void setUDPPort(int port)
	{
		udpPort = port;
	}

//overloaded function for comparison of two objects of type Client
	public boolean equals(Object other)
	{
		boolean status = false;
		
		if( ( this.myIP.equals(((Client)other).myIP) ) && (this.clientName.equals(((Client)other).clientName)) && (this.udpPort ==((Client)other).udpPort))
			status = true;
		return status; 
	}
	
	//----------------------toString-------------------------
	//	INPUTS: none
	//	OUTPUTS: Object's private data as strings
	//	DESCRIPTION: Prints out the object in string form
	//--------------------------------------------------------
	public String toString()
	{
		String result = "";
		result = "Client-Relayer " + clientName + " at " + myIP + " port " + udpPort;
		return result;
	}

	//--------------------------------disconnect---------------------------------------
	//	INPUTS: none
	//	OUTPUTS: none
	//	DESCRIPTION: sends the "I want to quit" message to the central server
	//		so that we successfully disconnect from the chat program
	//---------------------------------------------------------------------------
	public void disconnect(Socket socket, ObjectOutputStream output) throws IOException  
	{
		sendMessageToServer(socket, output, "***quit***");
		socket.close();
	}

//helper function that handles all of the rituals associated with initiating a connection to the CentralServer
	public void connectToServer( Socket socket, ObjectOutputStream output, ObjectInputStream input) throws IOException
	{
		Message m = null;
		sendMessageToServer(socket, output, "***connect***");
		output.writeObject(this); //send a copy of the object to the server to add to its list
		try
		{
			m = (Message) input.readObject();
		}
		catch(ClassNotFoundException cnfe) { cnfe.printStackTrace(); }	
		ClientGUI.updateTextArea(m);
	}
	
//helper function that handles all TCP message sending from the client to the central server
	public void sendMessageToServer(Socket socket, ObjectOutputStream output, String userMessage) throws IOException
	{
		Message m = null;
		///*ObjectOutputStream*/ output = new ObjectOutputStream (socket.getOutputStream());
		m = new Message(getName(), userMessage);
		output.writeObject(m);
	}

//helper function that handles all the UDP message sending between client-relayers 
	public void sendMessageToClient(DatagramSocket udpSocket, Client c, Message m)
	{
	
		try
		{
			udpSendObject(m, udpSocket, c.getIP(), c.getUDPPort());
		}
		catch( Exception e) { e.printStackTrace(); }
	}

//function that sends an Object (any type of object you want) to the designated UDP coordinates
//this function was derived from Ramesh Yerraballi's example code
	private void udpSendObject(Object obj, DatagramSocket dsock, InetAddress srvIP, int srvPort) throws IOException
	{
		ByteArrayOutputStream b_out = new ByteArrayOutputStream();
		ObjectOutputStream o_out     = new ObjectOutputStream(b_out);

		o_out.writeObject(obj);
		byte[] sStream = b_out.toByteArray();

		DatagramPacket sPacket = new DatagramPacket(sStream, sStream.length, srvIP, srvPort);
		dsock.send(sPacket);
	}
	
//function that receives an Object (any type of object you want) from the socket the program is listening for traffic on
//this function was derived from Ramesh Yerraballi's example code
	public static Object udpReceiveObject(DatagramSocket dsock) throws IOException
	{
		Object receivedObject = null;
		byte[] r_data;
		DatagramPacket r_packet;	
		ByteArrayInputStream b_in;
		r_data = new byte[10000]; // Allocate space for the packet conservatively estimate the size
		b_in = new ByteArrayInputStream(r_data);// As we want to unflatten an object from a byte array we 
							// wrap the byte array in a ByteArrayInputStream from
							// which we can read an unflattened object .
		r_packet = new DatagramPacket(r_data, r_data.length);
				
		try
		{
			dsock.receive(r_packet);
			ObjectInputStream o_in = new ObjectInputStream(b_in); // b_in contains the flattened message object
			// pass it to an ObjectInputStream so we can read
			receivedObject =  o_in.readObject();					// the message in an unflattened form
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return receivedObject;
	}

	//function that takes a received message and relays it to all the clients associated with this relayer
	//messageOriginator is an optional parameter, it will either have a value or be null
	public void relayChangesToClients(Client messageOriginator, WhiteboardChanges wbc , ArrayList<Client> clientList, DatagramSocket udpSocket)
	{
		
		if( messageOriginator != null)
		{
			for (Client cl : clientList)
			{
				if(cl.getName().equals(messageOriginator.getName()))//skip sending the message to the sender
					continue;
				else
				{
					try
					{
						udpSendObject( wbc , udpSocket, cl.getIP(), cl.getUDPPort());//send the message to the clients
					}
					catch(Exception e){ e.printStackTrace(); }
				}
			}
		}
		else
		{
			for (Client cl : clientList)
			{
				if(cl.getName().equals(wbc.getMessage().myName))//skip sending the message to the sender
					continue;
				else
				{
					try
					{
						udpSendObject(wbc, udpSocket, cl.getIP(), cl.getUDPPort());//send the message to the clients
					}
					catch(Exception e){ e.printStackTrace(); }
				}
			}
		}
	}
	
	public void sendWhiteboardChange(Tool.Shape s, Client client)
	{
		DatagramSocket udpSocket = ClientGUI.getUDPPort();
		try
		{
			udpSendObject(new WhiteboardChanges( s, new Message("WhiteboardChange", "")) , udpSocket, client.getIP(), client.getUDPPort());
		}
		catch( Exception e) { e.printStackTrace(); }
	}
		
}




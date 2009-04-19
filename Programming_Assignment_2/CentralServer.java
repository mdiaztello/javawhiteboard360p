/*-----------------CentralServeri-------------------------------
 *
 *
 * 	Author: Michael Diaz-Tello
 * 		with code derived from Ramesh Yerraballi's tcp and udp example code
 *
 * 	Description: the central server keeps a list of all the attached clients and
 * 	attached relayers that make up the chat room's network. It does all its client
 * 	communication as well as initial relayer communication via TCP. It does subsequent
 * 	relayer communication via udp.
 *
 *-------------------------------------------------------------*/



package Programming_Assignment_2;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
//this class basically just listens for traffic and then spawns a thread to handle that traffic
//and then it listens for more traffic
public class CentralServer
{

	static ArrayList<Client> clientList;

	public static void main(String[] args) throws IOException
	{
		ServerSocket WelcomeSocket;
		DatagramSocket udpSocket = new DatagramSocket();

		//ObjectInputStream input;
		//Message m;

		ArrayList<Client> clientList = new ArrayList<Client>();
		//m = null;
		WelcomeSocket = new ServerSocket(ChatElement.SERVER_PORT);
		while(true) //until the server is forcibly terminated, check to see if there are incoming TCP connections
		{
			Socket servantSocket = WelcomeSocket.accept();  //if there are incoming TCP connections, spawn a child process that will serve the
			Acceptor acceptor = new Acceptor(servantSocket, udpSocket, clientList);    //clients for as long as they want service
			new Thread(acceptor).start();	
		}
		//servantSocket = WelcomeSocket.accept();
	}
}


//class created for serving the actual client requests
class Acceptor implements Runnable
{
	private Message m;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private Socket servantSocket;
	private ArrayList<Client> clientList;
	private Client clientBeingServed;
	private DatagramSocket udpSocket;

	public Acceptor(Socket servantSock, DatagramSocket udpSock, ArrayList<Client> a)
	{
		//m =msg;
		//input = inputStream;
		servantSocket = servantSock;
		clientList = a;
		udpSocket = udpSock;
	}
	public void run()
	{
		try
		{
			System.out.println("\nClient connected\n");
			// Wrap the socket's input stream in a ObjectInputStream so we can read objects
			// as a whole rather than single bytes from the stream
			// TODO: I just changed the order of the below 2 statements, if it causes trouble, flip it back
			output = new ObjectOutputStream(servantSocket.getOutputStream());
			input = new ObjectInputStream(servantSocket.getInputStream());

			//System.out.println("we are here");//debug
			while(true)	
			{
				try
				{
					m = (Message) input.readObject();// Read a Message object from whoever is being served
				}
				catch(ClassNotFoundException e)
				{
					throw new IOException(e.getMessage());
				}
				catch(EOFException e2)
				{
					System.out.println("\nThe client has disconnected prematurely\n");
					break;	
				}

				System.out.println(m);//debug

				if ( m.myName.equals("Relayer") )
				{
					//serveRelayer();
					return; //does this terminate the thread?
				}
				else //we have a client talking to us
				{
					serveClient();
				}
			}
			//System.out.println("\nThe client has disconnected\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
//private helper function that handles requests from clients
	private void serveClient() throws IOException
	{
		if( m.chatMsg.equals("***quit***"))
		{
		//TODO REQUEST CS
			//check to see if the client wants to quit
				//if yes, remove client from client list, send a message to all relayers saying the client has quit
				//and send each relayer a new client list 
				//close the socket to the client
				//FIXME: THE ABOVE WILL REQUIRE SOME SORT OF MUTEX TO PROTECT THE CLIENT LIST
				
			System.out.println("The client has voluntarily disconnected\n");
			clientList.remove(clientList.indexOf(clientBeingServed));
			if(clientList.isEmpty())
				System.out.println("The client list is now empty\n");
			else
			{
				System.out.println("The client list is now the following:");
				for(Client client : clientList)
				{
					System.out.println(client.getName()); 
				}
				System.out.println("\n");
			}
			//send a message to the clients that the client has quit
			for(Client cli : clientList)
			{
				if( cli.equals(clientBeingServed))//skip notifying the quitter that he is quitting
					continue;

				try
				{
					udpSendObject(new WhiteboardChanges( null, new Message("CentralServer", "***disconnect client***") ),
						       	udpSocket, cli.getIP(), cli.getUDPPort());
				}
				catch(Exception e) {e.printStackTrace();}
				try
				{
					udpSendObject(clientBeingServed, udpSocket, cli.getIP(), cli.getUDPPort());
				}
				catch(Exception e) {e.printStackTrace();}
			}
			//TODO RELEASE CS
			servantSocket.close();
			return;
		}
		else if(m.chatMsg.equals("***connect***"))
		{
			
		//if the client doesn't want to quit, they are probably trying to join the chat
		//this is assumed because the only messages a client should send the CS are either a connect or disconnect message
			//if they are joining, add the client to the list, send a message to all relayers that a new client has joined
			//send each relayer a new client list
			//FIXME: THE ABOVE WILL REQUIRE SOME SORT OF MUTEX TO PROTECT THE CLIENT LIST

			clientBeingServed = null;
			try
			{
				clientBeingServed = (Client) input.readObject(); //receive the client object from the client program
			}
			catch( ClassNotFoundException cnfe)
			{
				throw new IOException(cnfe.getMessage());
			}
			catch(EOFException e2)
			{
				System.out.println("We have a problem!");
				System.exit(0);
			}
				//requestCS


			boolean status = addClientToList(clientBeingServed);

			if ( false == status )
				return; //stop running the thread because we were unable to add the client to the list

			//DEBUG
			System.out.println("The clientList is now the following:\n");
			for( Client client: clientList )
			{
				System.out.println( client.getName() );
			}

			
			output.writeObject(clientList);//give the client a list of all the current clients 

			for( Client cl: clientList)
			{	
				if(cl.equals(clientBeingServed))
					continue;
				else
				{
					try
					{
					//send command to each relayer to prepare to recieve an updated relayerList
						udpSendObject(new WhiteboardChanges( null, new Message("CentralServer", "***client connected***") ),
							       	udpSocket, cl.getIP(), cl.getUDPPort());
					}
					catch( Exception e) { e.printStackTrace(); }
					try
					{
						//send the updated relayerList
						udpSendObject(clientBeingServed, udpSocket, cl.getIP(), cl.getUDPPort());
					}
					catch( Exception e) { e.printStackTrace(); }
				}
			}
			
			//releaseCS
		}
		else 
		{//THIS SHOULD NEVER OCCUR, BUT WE WANT TO HANDLE STRANGE CASES
			System.out.println("Something strange happened");
			System.exit(-1);
		}
		
	}
	
//helper function that handles adding newly joined clients to the list of clients in the chat room
	public boolean addClientToList(Client clientBeingServed) throws IOException
	{
		if (clientBeingServed.getName().equals("CentralServer") || clientBeingServed.getName().equals("Relayer"))
		{
			output.writeObject(new Message( "CentralServer", "This name is reserved for use by the system, reconnect with a different name.\n"));
			servantSocket.close();
			return false;
		}
		if(clientList.isEmpty())
		{
			//modify clientList
			output.writeObject( new Message("CentralServer", "Welcome to the chat room, "
				+ clientBeingServed.getName() +"!"));
			clientList.add(clientBeingServed);	
		}
		else //the clientList is already populated and we must check to see if the potential client's name is already in use
		{
			int i;
			for( i = 0; i < clientList.size(); i++)
			{
				if( clientList.get(i).getName().equals(clientBeingServed.getName()) ) //compare name to list	
				{ //if the name is already in the list, we must tell the Client to pick a different name	
					
					output.writeObject( new Message( "CentralServer" , 
						"Name already in use, reconnect with a different name.\n"));	
					servantSocket.close();
					return false;
					//break; //no need to check anymore
				}
			}	
			if( i == clientList.size() ) //we didn't find the name in the clientList
			{
				output.writeObject( new Message("CentralServer", "Welcome to the chat room, "
					+ clientBeingServed.getName() +"!"));
				clientList.add(clientBeingServed);	
			}
			
		}

		return true;
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
}

/*-----------------------------Message--------------------------
 *
 *
 * class that represents the messages that are passed back and forth
 * between clients and the CS and the relayers
 *
 * --------------------------------------------------------------*/

package Whiteboard;

import java.io.Serializable;

//***********************************************
//Class: 		Message
//Description: 	chat message object
public class Message implements Serializable
{
	String myName;
	String chatMsg;
	
	// ***********************************************
	// constructor: Message
	// arguments: 	name string
	//				message string
	// description: initializes message object
	public Message(String n, String msg)
	{
		myName = new String(n);
		chatMsg = new String(msg);
	}
	// ***********************************************
	// method: 		toString
	// arguments: 	none
	// description: converts message object to a formatted string
	public String toString()
	{

		//System.out.println("I AM A MESSAGE OBJECT!");
		return myName + ": " + chatMsg;  
	}
}

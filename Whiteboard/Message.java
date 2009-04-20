/*-----------------------------Message--------------------------
 *
 *
 * class that represents the messages that are passed back and forth
 * between clients and the CS and the relayers
 *
 * --------------------------------------------------------------*/


package Whiteboard;
import java.io.*;
import java.net.*;

import java.io.Serializable;

public class Message implements Serializable
{
	String myName;
	String chatMsg;
	public Message(String n, String msg)
	{
		myName = new String(n);
		chatMsg = new String(msg);
	}
	public String toString()
	{

		//System.out.println("I AM A MESSAGE OBJECT!");
		return myName + ": " + chatMsg;  
	}
}

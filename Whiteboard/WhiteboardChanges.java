package Whiteboard;
import java.util.ArrayList;
import java.io.*;

//***********************************************
//Class: 		WhiteboardChanges
//Description:	class for object that stores whiteboard updates
public class WhiteboardChanges implements Serializable
{
	private Tool.Shape changeList; 	// a list of all the coordinates on the whiteboard that will turn white-->black
	private Message message; 		// a message in the text field

	// ***********************************************
	// constructor: WhiteboardChanges
	// arguments: 	list of shape updates
	//				updsocket to the client
	// description: server acceptor object
	public WhiteboardChanges(Tool.Shape c, Message m )
	{
		changeList = c;
		message = m;
	}
	// ***********************************************
	// method: 		getChangeList
	// arguments: 	none
	// description: returns list of updates
	public Tool.Shape getChangeList()
	{
		return changeList;
	}
	// ***********************************************
	// method: 		getMessage
	// arguments: 	none
	// description: returns message object
	public Message getMessage()
	{
		return message;
	}



}


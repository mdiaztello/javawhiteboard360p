
package Programming_Assignment_2;
import java.util.ArrayList;
import java.io.*;


public class WhiteboardChanges implements Serializable
{

	//private ArrayList<Coordinate> changeList; //a list of all the coordinates on the whiteboard that will turn white-->black
	private Tool.Shape changeList; //a list of all the coordinates on the whiteboard that will turn white-->black

	private Message message; // a message in the text field

	public WhiteboardChanges(Tool.Shape c, Message m )
	{
		changeList = c;
		message = m;
	}

	public Tool.Shape getChangeList()
	{
		return changeList;
	}

	public Message getMessage()
	{
		return message;
	}



}


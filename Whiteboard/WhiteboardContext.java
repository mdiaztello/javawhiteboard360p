package Whiteboard;

//***********************************************
//constructor: WhiteboardContext
//description: contains default values and methods for the whiteboard
public interface WhiteboardContext {

	public static final int WIDTH = 500, HEIGHT = 500;

	public int getXOffset();

	public int getYOffset();

	public void setXOffset(int newValue);

	public void setYOffset(int newValue);

	public String getText();
}

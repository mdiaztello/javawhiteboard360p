package Whiteboard;

import java.util.Vector;

//***********************************************
//Class: 		Transport
//Description:	Handles the addition of shapes and the status of the whiteboard
public abstract class Transport {

	protected Whiteboard wb;

	public void setWhiteboard(Whiteboard w) {
		wb = w;
	}

	protected void notifyWhiteboard() {
		wb.newShapesAvailable();
	}

	public abstract void addShape(Tool.Shape s);

	public abstract Vector getAllShapes();

	public abstract void start();

	public abstract void stop();

	public abstract String getStatus();
}

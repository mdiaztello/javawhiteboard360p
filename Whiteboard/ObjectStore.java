package Whiteboard;

import java.util.*;

//***********************************************
//Class: 		ObjectStore
//Description: 	ObjectStore maintains the list of all the shapes currently on the whiteboard
public class ObjectStore {

	private Vector v = new Vector();
	private boolean removeLast = false;
	private Whiteboard wb;

	// ***********************************************
	// method: 		getAllShapes
	// arguments: 	none
	// description: returns the vector of shapes
	public Vector getAllShapes() {
		return v;
	}

	// ***********************************************
	// method: 		add
	// arguments: 	the shape to be added or deleted from the vector
	// description: adds or removes a shape from the vector based on the first character of the argument given
	public boolean add(String shape) {
		if (removeLast) {
			removeLast = false;
			v.removeElementAt(v.size() - 1);
		}
		if (shape.startsWith("~")) {
			for (int i = v.size() - 1; i >= 0; i--) {
				Tool.Shape s = (Tool.Shape) v.elementAt(i);
				if (shape.equals("~" + s.shapeToString())) {
					v.removeElementAt(i);
					return true;
				}
			}
			System.out.println("Could not find shape: " + shape);
			return false;
		}
		Tool.Shape ss = Tool.getShapeFromString(shape);
		if (ss == null) {
			System.out.println("Unknown shape: " + shape);
			return false;
		} else {
			v.addElement(ss);
			return true;
		}
	}

	/*public void addTemp(String shape) {
		if (shape.startsWith("~"))
			return;
		if (!removeLast) {
			Tool.Shape ss = Tool.getShapeFromString(shape);
			if (ss == null)
				return;
			removeLast = true;
			v.addElement(ss);
		}
	}*/

	// ***********************************************
	// method: 		clear
	// arguments: 	none
	// description: clears the vector of all shapes
	public void clear() {
		v.clear();
	}
}

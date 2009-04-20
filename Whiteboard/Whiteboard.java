package Whiteboard;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

//***********************************************
//Class: 		Whiteboard
//Description: 	Whiteboard builds and updates the whiteboard based on mouse and key events
public class Whiteboard extends Canvas implements MouseListener,
		MouseMotionListener, KeyListener, ActionListener, WhiteboardContext {

	private Image buffer, contentBuffer;

	public static final int WIDTH = WhiteboardContext.WIDTH,
			HEIGHT = WhiteboardContext.HEIGHT;

	private int currentColor = 0, currentTool = 1;

	private int xOffset = 0, yOffset = 0;

	private Tool.Shape currentShape = null;

	private Transport tr;

	private TextField tf;

	private boolean readOnly;

	private boolean repaint = true;

	private Vector myShapes = new Vector();
	
	public void setRepaint(boolean b) {
		repaint = b;
	}
	
	private static final Cursor SELECT_CURSOR = new Cursor(
			Cursor.DEFAULT_CURSOR), DRAW_CURSOR = new Cursor(
			Cursor.CROSSHAIR_CURSOR);

	public int getXOffset() {
		return xOffset;
	}

	public int getYOffset() {
		return yOffset;
	}

	public void setXOffset(int newValue) {
		xOffset = newValue;
	}

	public void setYOffset(int newValue) {
		yOffset = newValue;
	}

	public String getText() {
		return tf.getText();
	}

	private static final Color[] COLORS = { Color.black, Color.gray,
			Color.lightGray, Color.white, Color.red, Color.orange,
			Color.yellow, Color.green, Color.cyan, Color.blue, Color.magenta, };

	// ***********************************************
	// constructor: Whiteboard
	// arguments:  	local event tracker
	//				enable/disable read only
	// description: enables listeners for this instance of whiteboard
	public Whiteboard(Transport t, boolean readOnly) {
		tr = t;
		this.readOnly = readOnly;
		tr.setWhiteboard(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		setCursor(DRAW_CURSOR);
	}

	// ***********************************************
	// method: 		buildWhiteboard
	// arguments: 	frame containing title
	// description: method that constructs the whiteboard
	public void buildWhiteboard(Container topPanel) {
		Panel bottomPanel 		= new Panel(new BorderLayout()), 
			  bottomPanelFields = new Panel(new BorderLayout());
		topPanel.setBackground(Color.white);
		bottomPanel.setBackground(Color.DARK_GRAY);
		topPanel.setLayout(new BorderLayout(5, 5));
		topPanel.add("North", this);
		topPanel.add("Center", bottomPanel);
		bottomPanel.add("North", bottomPanelFields);
		bottomPanelFields.add("West", new Label("   Text:"));
		// String sample="Java-Version: "+System.getProperty("java.version");
		// String sample="MaxMemory: "+Runtime.getRuntime().maxMemory();
		bottomPanelFields.add("Center", tf = new TextField(""));
		/*Button undoButton;
		bottomPanelFields.add("East", undoButton = new Button("Undo!"));
		undoButton.addActionListener(this);*/
		if (readOnly) {
			tf.setText("Nur Lesezugriff gestattet");
			tf.setEditable(false);
		}
	}

	// ***********************************************
	// method: 		getPreferredSize
	// arguments: 	nonee
	// description: returns the size of the canvas area
	public Dimension getPreferredSize() {
		return new Dimension(WIDTH + 20, HEIGHT);
	}

	/** @deprecated */
	public boolean isFocusTraversable() {
		return true;
	}

	public boolean isFocusable() {
		return true;
	}

	// ***********************************************
	// method: 		update
	// arguments: 	abstract object to handle graphics
	// description: updates the current state of the whiteboard
	public void update(Graphics g) {
		if (buffer == null)
			buffer = createImage(getSize().width, getSize().height);
		Graphics bufg = buffer.getGraphics();
		paint(bufg);
		g.drawImage(buffer, 0, 0, this);
	}

	// ***********************************************
	// method: 		paint
	// arguments: 	abstract object to handle graphics
	// description: creates the layout of the buttons and text field on the whiteboard
	public void paint(Graphics g) {
		if (g == null)
			return;
		paintContents(g);
		g.setColor(Color.black);
		g.fillRect(20, 0, 1, HEIGHT);
		g.setColor(Color.white);
		g.fillRect(0, 0, 20, HEIGHT);
		if (readOnly)
			return;
		for (int i = 0; i < Tool.TOOLS.length; i++) {
			Tool.TOOLS[i].paint(g, i, currentTool == i);
		}
		int cnt = -1;
		for (int i = HEIGHT - 20 * COLORS.length; i < HEIGHT; i += 20) {
			cnt++;
			Color c = COLORS[cnt];
			g.setColor(c);
			g.fillRect(0, i, 20, 20);
			if (cnt == currentColor) {
				c = (c == Color.black ? Color.white : Color.black);
				g.setColor(c);
				g.drawRect(2, i + 2, 16, 16);
			}
		}
	}

	// ***********************************************
	// method: 		paintContents
	// arguments: 	abstract object to handle graphics
	// description: paints shape onto whiteboard if shape is present in buffer
	public void paintContents(Graphics g) {
		if (contentBuffer == null)
			contentBuffer = createImage(getSize().width, getSize().height);
		if (currentShape != null && currentShape.needsRepaint())
			repaint = true;
		if (repaint) {
			paintContents2(contentBuffer.getGraphics());
			repaint = false;
		}
		g.drawImage(contentBuffer, 0, 0, this);
		if (currentShape != null)
			currentShape.paint(this, g);
	}

	// ***********************************************
	// method: 		paintContents2
	// arguments: 	abstract object to handle graphics
	// description: clears the whiteboard and repaints all shapes
	public void paintContents2(Graphics g) {
		g.clearRect(0, 0, WIDTH + 20, HEIGHT);
		xOffset = yOffset = 0;
		Vector shapes = tr.getAllShapes();
		Enumeration e = shapes.elements();
		while (e.hasMoreElements()) {
			Tool.Shape s = (Tool.Shape) e.nextElement();
			s.updateState(this);
		}
		if (currentShape != null)
			currentShape.updateState(this);
		e = shapes.elements();
		while (e.hasMoreElements()) {
			Tool.Shape s = (Tool.Shape) e.nextElement();
			s.paint(this, g);
		}
	}

	// ***********************************************
	// method: 		addShape
	// arguments: 	the shape to be added to the buffer
	// description: adds the shape to the buffer
	public void addShape(Tool.Shape s) {
		myShapes.addElement(s);
		tr.addShape(s);
	}

	// ***********************************************
	// method: 		newShapesAvailable
	// arguments: 	none
	// description: if new shape are in the buffer, repaint the whiteboard
	public void newShapesAvailable() {
		repaint = true;
		repaint();
	}

	// Mouse events

	// ***********************************************
	// method: 		mousePressed
	// arguments: 	mouse event handler 
	// description: updates whiteboard based on position of mouse when mouse button is pressed
	public void mousePressed(MouseEvent evt) {
		if (readOnly)
			return;
		int x = evt.getX();
		int y = evt.getY();
		if (x <= 20) { // Icon bar clicked
			if (y >= HEIGHT - 20 * COLORS.length) {// Color selector
				currentColor = (y - HEIGHT + 20 * COLORS.length) / 20;
				repaint = true;
			} else if (y < Tool.TOOLS.length * 20) {
				currentTool = y / 20;
				repaint = true;
			} else {
				System.out.println("Nothing here");
			}
		} else {
			if (currentShape != null) {
				// Multi click --> cancel
				currentShape = null;
			} else {
				currentShape = Tool.TOOLS[currentTool].createShape(this, x
						- xOffset, y - yOffset, COLORS[currentColor]);
			}
		}
		repaint();
	}

	// ***********************************************
	// method: 		mouseDragged
	// arguments: 	mouse event handler 
	// description: keeps track of mouse position while mouse button is pressed and has not been released
	public void mouseDragged(MouseEvent evt) {
		if (readOnly)
			return;
		int x = evt.getX();
		int y = evt.getY();
		if (currentShape != null) {
			currentShape.updatePoint(this, x - xOffset, y - yOffset);
			repaint();
		}
	}

	// ***********************************************
	// method: 		mouseReleased
	// arguments: 	mouse event handler 
	// description: adds the drawn shape to the buffer and sends change to all other clients on button release
	public void mouseReleased(MouseEvent evt) {
		if (readOnly)
			return;
		int x = evt.getX();
		int y = evt.getY();
		if (currentShape != null) {
			currentShape.endPoint(this, x - xOffset, y - yOffset);
			addShape(currentShape);
			Client ourClient = ClientGUI.getClient();
			//System.out.println("THIS IS OUR CLIENT: "+ ourClient);
			for (Client client : ClientGUI.getClientList())
			{
				if (client.equals(ourClient))
				{
					//System.out.println("THEY ARE THE SAME");
				}
				else
				{
					ourClient.sendWhiteboardChange(currentShape, client);
					//System.out.println("CURRENT CLIENT: " + client);
				}
				
			}
			
			currentShape = null;
			repaint = true;
			repaint();
		}
	}

	// ***********************************************
	// method: 		mouseMoved
	// arguments: 	mouse event handler 
	// description: changes cursor based on cursor position
	public void mouseMoved(MouseEvent evt) {
		int x = evt.getX();
		if (x <= 20) { // Icon bar
			setCursor(SELECT_CURSOR);
		} else {
			setCursor(DRAW_CURSOR);
		}
	}

	public void mouseEntered(MouseEvent evt) {
	}

	public void mouseExited(MouseEvent evt) {
	}

	public void mouseClicked(MouseEvent evt) {
	}

	// keyboard events

	// ***********************************************
	// method: 		keyPressed
	// arguments: 	keyboard event handler 
	// description: cancels the drawing of a shape if ESC is pressed
	public void keyPressed(KeyEvent evt) {
		if (readOnly)
			return;
		if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
			currentShape = null;
			repaint();
		}
	}

	public void keyReleased(KeyEvent evt) {
	}

	// ***********************************************
	// method: 		keyTyped
	// arguments: 	keyboard event handler 
	// description: allows user to access toolbar using the keyboard
	public void keyTyped(KeyEvent evt) {
		if (readOnly)
			return;
		char c = evt.getKeyChar();
		if (c >= '1' && c < '1' + COLORS.length) { // color
			currentColor = c - '1';
			repaint();
		} else {
			for (int i = 0; i < Tool.TOOLS.length; i++) {
				if (Tool.TOOLS[i].getToolID() == c) {
					currentTool = i;
					repaint();
				}
			}
		}
	}

	// Action listener
	
	// ***********************************************
	// method: 		actionPerformed
	// arguments: 	action event handler 
	// description: removes the last drawn shaped from the whiteboard if 'undo' button pressed
	public void actionPerformed(ActionEvent evt) {
		//if (myShapes.size() == 0)
			return;
		/*Tool.Shape lastShape = (Tool.Shape) myShapes
				.elementAt(myShapes.size() - 1);
		tr.addShape(new Tool.DeleteShape(lastShape));
		myShapes.removeElementAt(myShapes.size() - 1);*/
	}
}

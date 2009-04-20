package Whiteboard;

import java.awt.*;

public  class EraseTool extends Tool {

    public void paint(Graphics g, int pos, boolean active) {
	super.paint(g, pos, active);
    }

    public Shape createShape(WhiteboardContext w, int x, int y, Color c) {
	return new FilledBoxShape(x,y,c);
    }

    public Shape shapeFromString(String s) {
	if (s.charAt(0) != getToolID()) return null;
	int[] ts = tokenize(s.substring(1));
	return new FilledBoxShape(ts[0], ts[1], ts[2], ts[3],
				  new Color(ts[4]));
    }


    public char getToolID() {
	return 'Q';
    }

    public static class FilledBoxShape extends TwoPointShape {

	public FilledBoxShape(int x, int y, Color c) {
	    super(x,y,c);
	}
	
	public FilledBoxShape(int x1, int y1, int x2, int y2, Color c) {
	    super(20,0,WhiteboardContext.WIDTH, WhiteboardContext.HEIGHT, Color.white);
	    this.c=Color.white;
	    this.x1=20;
	    this.y1=0;
	    this.x2=WhiteboardContext.WIDTH;
	    this.y2=WhiteboardContext.HEIGHT;
	}
		
	public void paint(WhiteboardContext w, Graphics g) {
	    int offsX=w.getXOffset();
	    int offsY=w.getYOffset();
	    int xx1=x1, xx2=x2, yy1=y1, yy2=y2;
	    if (xx1 > xx2) {
		xx1=x2; xx2=x1;
	    }
	    if (yy1 > yy2) {
		yy1=y2; yy2=y1;
	    }
	    //g.setColor(c);
	    g.setColor(Color.white);
	    //g.fillRect(xx1+offsX, yy1+offsY, xx2-xx1, yy2-yy1);
	    
		g.fillRect(20, 0, WhiteboardContext.WIDTH, WhiteboardContext.HEIGHT);
	}

	public char getToolID() {
	    return 'Q';
	}
    }
}




package Programming_Assignment_2;
import java.io.Serializable;

public class Coordinate implements Serializable
{

	private int x,y;

	public Coordinate( int a, int b)
	{
		x = a;
		y = b;
	}
	public int getX()
	{
		return x;
	}
 	public int getY()
	{
		return y;
	}


}

package sg.money.widgets;

public class PieChartSegment
{
	private float m_angle;
	private int m_color;
	
	
	/* Constructor */
	
	public PieChartSegment()
	{
	}
	
	
	/* Getters / setters */

	public void setAngle(float angle)
	{
		this.m_angle = angle;
	}

	public float getAngle()
	{
		return m_angle;
	}

	public void setColor(int color)
	{
		this.m_color = color;
	}

	public int getColor()
	{
		return m_color;
	}
}

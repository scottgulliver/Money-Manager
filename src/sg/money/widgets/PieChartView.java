package sg.money.widgets;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import sg.money.widgets.PieChartSegment;

public class PieChartView extends View {
	private Paint m_backgroundPaint;
	private Rect m_entireRect;
	private ArrayList<PieChartSegment> m_segments;
	
	
	/* Constructor */
	
	public PieChartView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }
	
	
	/* Methods */
	
	private void init() {
		m_backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_backgroundPaint.setStyle(Paint.Style.FILL);
		m_backgroundPaint.setColor(Color.argb(0, 241, 241, 241));
		m_backgroundPaint.setColor(Color.argb(255, 255, 255, 255));
		
		m_entireRect = new Rect();
		
		m_segments = new ArrayList<PieChartSegment>();
	}
	
	public void setSegments(ArrayList<PieChartSegment> segments)
	{
		m_segments = segments;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		m_entireRect = new Rect(getPaddingLeft(), getPaddingTop(), w-getPaddingRight(), h-getPaddingBottom());
	}
	
	private PointF rotatePoint(PointF point, PointF origin, float angle)
	{
		PointF rotatedPoint = new PointF();
		rotatedPoint.x = (float) (Math.cos(angle) * (point.x-origin.x) - Math.sin(angle) * (point.y-origin.y) + origin.x);
		rotatedPoint.y = (float) (Math.sin(angle) * (point.x-origin.x) + Math.cos(angle) * (point.y-origin.y) + origin.y);
		return rotatedPoint;
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		canvas.drawRect(m_entireRect, m_backgroundPaint);
		
		PointF centre = new PointF(m_entireRect.exactCenterX(),m_entireRect.exactCenterY());
		float extentDistance = centre.x * 0.8f;
		RectF shapeRect = new RectF(centre.x-extentDistance, centre.y-extentDistance, centre.x+extentDistance, centre.y+extentDistance);
		
		PointF standard = new PointF(centre.x, centre.y-extentDistance);
		
		Paint outlinePaint = new Paint();
		outlinePaint.setStyle(Paint.Style.STROKE);
		outlinePaint.setStrokeWidth(5.0f);
		outlinePaint.setAntiAlias(true);
		outlinePaint.setColor(Color.argb(255, 50, 50, 50));
		outlinePaint.setColor(Color.argb(255, 255, 255, 255));
		
		//draw segments
		
		float totalAngle = -90.0f;
		for(PieChartSegment segment : m_segments)
		{
			Path path = new Path();
			path.moveTo(centre.x, centre.y);
			PointF from = rotatePoint(standard, centre, totalAngle);
			path.lineTo(from.x, from.y);
			path.addArc(shapeRect, totalAngle, segment.getAngle());
			path.lineTo(centre.x, centre.y);
			path.close(); 
			Paint pieChartPaint = new Paint();
			pieChartPaint.setStyle(Paint.Style.FILL);
			pieChartPaint.setColor(segment.getColor());
			canvas.drawPath(path, pieChartPaint);
			totalAngle = totalAngle + segment.getAngle();
		}
		
		//draw segment outlines
		totalAngle = 0.0f;
		if (m_segments.size() > 1)
		{
			for(PieChartSegment segment : m_segments)
			{ 
				PointF toPoint = rotatePoint(standard, centre, (float)Math.toRadians(totalAngle + segment.getAngle()));
	
				Path path2 = new Path();
				path2.moveTo(centre.x, centre.y);
				path2.lineTo(toPoint.x, toPoint.y);  
				path2.close();
				canvas.drawPath(path2, outlinePaint);
				
				totalAngle = totalAngle + segment.getAngle();
			}
		}
		
		//draw outline
		Path path = new Path();
		path.addArc(shapeRect, 0, 360);
		path.close();
		outlinePaint.setStrokeWidth(5.0f);
		canvas.drawPath(path, outlinePaint);
		
		//draw inner circle
		m_backgroundPaint.setColor(Color.argb(255, 241, 241, 241));
		m_backgroundPaint.setColor(Color.argb(255, 255, 255, 255));
		canvas.drawCircle(centre.x, centre.y, extentDistance/5.0f, m_backgroundPaint);
		canvas.drawCircle(centre.x, centre.y, extentDistance/5.0f, outlinePaint);
		m_backgroundPaint.setColor(Color.argb(0, 241, 241, 241));
		m_backgroundPaint.setColor(Color.argb(255, 255, 255, 255));
	}
}

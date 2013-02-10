package sg.money;

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

public class PieChartView extends View
{
	Paint backgroundPaint = new Paint();
	Rect entireRect = new Rect();
	ArrayList<PieChartSegment> segments = new ArrayList<PieChartSegment>();
	
	public PieChartView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }
	
	private void init() {
		backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setColor(Color.argb(0, 241, 241, 241));
		backgroundPaint.setColor(Color.argb(255, 255, 255, 255));
		
		entireRect = new Rect();
	}
	
	public void setSegments(ArrayList<PieChartSegment> segments)
	{
		this.segments = segments;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		entireRect = new Rect(getPaddingLeft(), getPaddingTop(), w-getPaddingRight(), h-getPaddingBottom());
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
		canvas.drawRect(entireRect, backgroundPaint);
		
		PointF centre = new PointF(entireRect.exactCenterX(),entireRect.exactCenterY());
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
		for(PieChartSegment segment : segments)
		{
			Path path = new Path();
			path.moveTo(centre.x, centre.y);
			PointF from = rotatePoint(standard, centre, totalAngle);
			path.lineTo(from.x, from.y);
			path.addArc(shapeRect, totalAngle, segment.angle);
			path.lineTo(centre.x, centre.y);
			path.close(); 
			Paint pieChartPaint = new Paint();
			pieChartPaint.setStyle(Paint.Style.FILL);
			pieChartPaint.setColor(segment.color);
			canvas.drawPath(path, pieChartPaint);
			totalAngle = totalAngle + segment.angle;
		}
		
		//draw segment outlines
		totalAngle = 0.0f;
		if (segments.size() > 1)
		{
			for(PieChartSegment segment : segments)
			{ 
				PointF toPoint = rotatePoint(standard, centre, (float)Math.toRadians(totalAngle + segment.angle));
				
				/*Path path = new Path();
				path.moveTo(centre.x, centre.y); 
				path.lineTo(fromPoint.x, fromPoint.y);
				path.close();
				canvas.drawPath(path, outlinePaint);*/
	
				Path path2 = new Path();
				path2.moveTo(centre.x, centre.y);
				path2.lineTo(toPoint.x, toPoint.y);  
				path2.close();
				canvas.drawPath(path2, outlinePaint);
				
				totalAngle = totalAngle + segment.angle;
			}
		}
		
		//draw outline
		Path path = new Path();
		path.addArc(shapeRect, 0, 360);
		path.close();
		outlinePaint.setStrokeWidth(5.0f);
		canvas.drawPath(path, outlinePaint);
		
		//draw inner circle
		backgroundPaint.setColor(Color.argb(255, 241, 241, 241));
		backgroundPaint.setColor(Color.argb(255, 255, 255, 255));
		canvas.drawCircle(centre.x, centre.y, extentDistance/5.0f, backgroundPaint);
		canvas.drawCircle(centre.x, centre.y, extentDistance/5.0f, outlinePaint);
		backgroundPaint.setColor(Color.argb(0, 241, 241, 241));
		backgroundPaint.setColor(Color.argb(255, 255, 255, 255));
	}
}

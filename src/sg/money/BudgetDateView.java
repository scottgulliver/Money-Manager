package sg.money;

import java.text.DecimalFormat;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class BudgetDateView extends View
{
	Paint backgroundPaint = new Paint();
	Paint foregroundPaint = new Paint();
	Paint foregroundOverPaint = new Paint();
	Paint textPaint = new Paint();
	Rect entireRect = new Rect();
	Rect foregroundRect = new Rect();
	int fontSize = 30;
	
	private double budget = 30;
	private double value;
	
	public BudgetDateView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }
	
	private void init() {
		backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setColor(Color.argb(255, 171, 202, 217));
		
		foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		foregroundPaint.setStyle(Paint.Style.FILL);
		foregroundPaint.setColor(Color.argb(255, 87, 183, 87));
		
		foregroundOverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		foregroundOverPaint.setStyle(Paint.Style.FILL);
		foregroundOverPaint.setColor(Color.argb(255, 183, 87, 87));
		
		textPaint = new Paint();
		Typeface tf = Typeface.create("Helvetica",Typeface.NORMAL);
		textPaint.setColor(Color.argb(255, 50, 50, 50));
		textPaint.setTextSize(fontSize);
		textPaint.setTypeface(tf);
		
		entireRect = new Rect();
		foregroundRect = new Rect();
		
		if (isInEditMode())
		{
			budget = 100;
			value = 66;
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
       float xpad = (float)(getPaddingLeft() + getPaddingRight());
       float internalWidth = (float)w - xpad;
       int width = (int)(internalWidth * (getPercentageComplete() * 0.01));

		entireRect = new Rect(getPaddingLeft(), getPaddingTop(), w-getPaddingRight(), h-getPaddingBottom()- fontSize - 1);
		foregroundRect = new Rect(getPaddingLeft(), getPaddingTop(), getPaddingLeft()+width, h-getPaddingBottom()- fontSize - 1);
		if (width > 0)
			entireRect.left = foregroundRect.right + (int)Misc.dipsToPixels(getResources(), 2);
	}
	
	private double getPercentageComplete()
	{
		double percentageConversion = 100.0 / budget;
		return percentageConversion * value;
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		canvas.drawRect(entireRect, backgroundPaint);
		canvas.drawRect(foregroundRect, value <= budget ? foregroundPaint : foregroundOverPaint);
		
		DecimalFormat df = new DecimalFormat("#");
		String percentageString = df.format(getPercentageComplete()) + "%";
		float textWidth = textPaint.measureText(percentageString);
		
		float xPos;
		if (foregroundRect.right > entireRect.right)
		{
			xPos = entireRect.right - Misc.dipsToPixels(getResources(), 2) - textWidth;
		}
		else if (textWidth + Misc.dipsToPixels(getResources(), 4) <= foregroundRect.width())
		{ 
			xPos = foregroundRect.width() - textWidth - Misc.dipsToPixels(getResources(), 2);
		}
		else
		{
			xPos = foregroundRect.width() + Misc.dipsToPixels(getResources(), 4);
		}
		
		Rect bounds = new Rect();
		textPaint.getTextBounds(percentageString, 0, percentageString.length(), bounds);
		float yPos = entireRect.top + ((float)entireRect.height() / 2.0f) + ((float)bounds.height() / 2.0f);
		canvas.drawText(percentageString, xPos, yPos, textPaint);
	}
	
	public void setBudget(double budget)
	{
		this.budget = budget;
	}
	
	public void setToDate(double value)
	{
		this.value = value;
	}
}

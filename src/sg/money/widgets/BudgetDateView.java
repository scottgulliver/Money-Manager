package sg.money.widgets;

import java.text.DecimalFormat;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import sg.money.utils.Misc;

public class BudgetDateView extends View {

	private Paint m_backgroundPaint;
	private Paint m_foregroundPaint;
	private Paint m_foregroundOverPaint;
	private Paint m_textPaint;
	private Rect m_entireRect;
	private Rect m_foregroundRect;
	private double m_budget;
	private double m_value;
	
	private final int FONTSIZE = 30;
	
	
	/* Constructor */
	
	public BudgetDateView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }
	
	
	/* Methods */
	
	private void init() {
		m_backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_backgroundPaint.setStyle(Paint.Style.FILL);
		m_backgroundPaint.setColor(Color.argb(255, 171, 202, 217));
		
		m_foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_foregroundPaint.setStyle(Paint.Style.FILL);
		m_foregroundPaint.setColor(Color.argb(255, 87, 183, 87));
		
		m_foregroundOverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_foregroundOverPaint.setStyle(Paint.Style.FILL);
		m_foregroundOverPaint.setColor(Color.argb(255, 183, 87, 87));
		
		m_textPaint = new Paint();
		Typeface tf = Typeface.create("Helvetica",Typeface.NORMAL);
		m_textPaint.setColor(Color.argb(255, 50, 50, 50));
		m_textPaint.setTextSize(FONTSIZE);
		m_textPaint.setTypeface(tf);
		
		m_entireRect = new Rect();
		m_foregroundRect = new Rect();
		
		if (isInEditMode())
		{
			m_budget = 100;
			m_value = 66;
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
       float xpad = getPaddingLeft() + getPaddingRight();
       float internalWidth = (float)w - xpad;
       int width = (int)(internalWidth * (getPercentageComplete() * 0.01));

		m_entireRect = new Rect(getPaddingLeft(), getPaddingTop(), w-getPaddingRight(), h-getPaddingBottom()- FONTSIZE - 1);
		m_foregroundRect = new Rect(getPaddingLeft(), getPaddingTop(), getPaddingLeft()+width, h-getPaddingBottom()- FONTSIZE - 1);
		if (width > 0)
		{
			m_entireRect.left = m_foregroundRect.right + (int) Misc.dipsToPixels(getResources(), 2);
		}
	}
	
	private double getPercentageComplete()
	{
		double percentageConversion = 100.0 / m_budget;
		return percentageConversion * m_value;
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		canvas.drawRect(m_entireRect, m_backgroundPaint);
		canvas.drawRect(m_foregroundRect, m_value <= m_budget ? m_foregroundPaint : m_foregroundOverPaint);
		
		DecimalFormat df = new DecimalFormat("#");
		String percentageString = df.format(getPercentageComplete()) + "%";
		float textWidth = m_textPaint.measureText(percentageString);
		
		float xPos;
		if (m_foregroundRect.right > m_entireRect.right)
		{
			xPos = m_entireRect.right - Misc.dipsToPixels(getResources(), 2) - textWidth;
		}
		else if (textWidth + Misc.dipsToPixels(getResources(), 4) <= m_foregroundRect.width())
		{ 
			xPos = m_foregroundRect.width() - textWidth - Misc.dipsToPixels(getResources(), 2);
		}
		else
		{
			xPos = m_foregroundRect.width() + Misc.dipsToPixels(getResources(), 4);
		}
		
		Rect bounds = new Rect();
		m_textPaint.getTextBounds(percentageString, 0, percentageString.length(), bounds);
		float yPos = m_entireRect.top + ((float)m_entireRect.height() / 2.0f) + ((float)bounds.height() / 2.0f);
		canvas.drawText(percentageString, xPos, yPos, m_textPaint);
	}
	
	public void setBudget(double budget)
	{
		this.m_budget = budget;
	}
	
	public void setToDate(double value)
	{
		this.m_value = value;
	}
}

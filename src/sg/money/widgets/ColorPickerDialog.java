package sg.money.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import sg.money.utils.Misc;

public class ColorPickerDialog extends Dialog {

	public interface OnColorChangedListener {
		void colorChanged(int color);
	}

	private OnColorChangedListener m_listener;
	private int m_initialColor;

	private static class ColorPickerView extends View {
		
		private Paint m_paint;
		private Paint m_paintBlackWhite;
		private Paint m_oldPaint;
		private Paint m_newPaint;
		private Paint m_highlightPaint;
		private final int[] m_colors;
		private final int[] m_colorsBlackWhite;
		private OnColorChangedListener m_listener;
		private Rect m_gradientRect;
		private Rect m_oldRect;
		private Rect m_newRect;
		private boolean m_onOld, m_onNew, m_onSelector;
		private boolean m_highlightOld, m_highlightNew;
		
		
		/* Constructor */

		ColorPickerView(Context c, OnColorChangedListener l, int color) {
			super(c);
			m_listener = l;
			m_colors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
					0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };

			m_colorsBlackWhite = new int[] { 0xFF000000, 0x00AAAAAA, 0xFFFFFFFF };

			Shader s = new LinearGradient(0, 0, 0, 0, m_colors, null, Shader.TileMode.CLAMP);
			Shader sBlackWhite = new LinearGradient(0, 0, 0, 0, m_colorsBlackWhite, null, Shader.TileMode.CLAMP);

			m_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			m_paint.setShader(s);
			m_paint.setStyle(Paint.Style.FILL);

			m_paintBlackWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
			m_paintBlackWhite.setShader(sBlackWhite);
			m_paintBlackWhite.setStyle(Paint.Style.FILL);

			m_oldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			m_oldPaint.setColor(color);
			m_oldPaint.setStyle(Paint.Style.FILL);

			m_newPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			m_newPaint.setColor(color);
			m_newPaint.setStyle(Paint.Style.FILL);

			m_highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			m_highlightPaint.setColor(Color.WHITE);
			m_highlightPaint.setStyle(Paint.Style.STROKE);
			m_highlightPaint.setStrokeWidth(5);
		}

		
		/* Methods */

		@Override
		protected void onDraw(Canvas canvas) {

			canvas.drawRect(m_gradientRect, m_paint);
			canvas.drawRect(m_gradientRect, m_paintBlackWhite);

			canvas.drawRect(m_oldRect, m_oldPaint);
			canvas.drawRect(m_newRect, m_newPaint);

			if (m_highlightOld)
			{
				canvas.drawRect(m_oldRect, m_highlightPaint);
			}
			if (m_highlightNew)
			{
				canvas.drawRect(m_newRect, m_highlightPaint);
			}
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			w = this.getWidth();
			int fiveDP = (int) Misc.dipsToPixels(getContext().getResources(), 5);
			int tenDP = (int) Misc.dipsToPixels(getContext().getResources(), 10);
			int fiftyDP = (int) Misc.dipsToPixels(getContext().getResources(), 50);
			m_gradientRect = new Rect(tenDP, tenDP, w - tenDP, h - fiftyDP - tenDP);

			m_oldRect = new Rect(tenDP, h - fiftyDP, (m_gradientRect.width() / 2) + fiveDP, h - tenDP);
			m_newRect = new Rect(m_oldRect.right + tenDP, h - fiftyDP, m_gradientRect.right, h - tenDP);

			Shader s = new LinearGradient(m_gradientRect.left, m_gradientRect.top,
					m_gradientRect.right, m_gradientRect.top, m_colors, null,
					Shader.TileMode.CLAMP);
			m_paint.setShader(s);

			Shader sBlackWhite = new LinearGradient(m_gradientRect.left, m_gradientRect.top,
					m_gradientRect.left, m_gradientRect.bottom, m_colorsBlackWhite, null,
					Shader.TileMode.CLAMP);
			m_paintBlackWhite.setShader(sBlackWhite);
		};

		private static int ave(int s, int d, float p) {
			return s + Math.round(p * (d - s));
		}

		private static int interpColor(int colors[], float unit) {
			if (unit <= 0)
			{
				return colors[0];
			}
			if (unit >= 1)
			{
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			int i = (int) p;
			p -= i;

			// p is just the fractional part [0...1) and i is the index
			int c0 = colors[i];
			int c1 = colors[i + 1];
			int a = ave(Color.alpha(c0), Color.alpha(c1), p);
			int r = ave(Color.red(c0), Color.red(c1), p);
			int g = ave(Color.green(c0), Color.green(c1), p);
			int b = ave(Color.blue(c0), Color.blue(c1), p);

			return Color.argb(a, r, g, b);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			boolean onSelector = m_gradientRect.contains((int) x, (int) y);
			boolean onOld = m_oldRect.contains((int) x, (int) y);
			boolean onNew = m_newRect.contains((int) x, (int) y);

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				m_onOld = onOld;
				m_onNew = onNew;
				m_onSelector = onSelector;

				if (m_onOld) {
					m_highlightOld = true;
					invalidate();
					break;
				}
				if (m_onNew) {
					m_highlightNew = true;
					invalidate();
					break;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (m_onOld) {
					if (m_highlightOld != onOld) {
						m_highlightOld = onOld;
						invalidate();
					}
				} else if (m_onNew) {
					if (m_highlightNew != onNew) {
						m_highlightNew = onNew;
						invalidate();
					}
				} else if (m_onSelector && onSelector) {
					x -= m_gradientRect.left;
					x = (x / (float) m_gradientRect.width());
					y -= m_gradientRect.top;
					y = (y / (float) m_gradientRect.height());
					int new1 = interpColor(m_colors, x);
					int addColor = (int)((510f * y) - 255f);
					m_newPaint.setColor(Color.argb(
							255,
							clamp(Color.red(new1) + addColor, 0, 255),
							clamp(Color.green(new1) + addColor, 0, 255),
							clamp(Color.blue(new1) + addColor, 0, 255)));
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (m_onNew) {
					if (onNew) {
						m_listener.colorChanged(m_newPaint.getColor());
					}
				} else if (m_onOld) {
					if (onOld) {
						m_listener.colorChanged(m_oldPaint.getColor());
					}
				}

				m_onOld = false;
				m_onNew = false;
				m_onSelector = false;
				m_highlightOld = false;
				m_highlightNew = false;
				invalidate();
				break;
			}
			return true;
		}
	}
	
	private static int clamp(int source, int min, int max)
	{
		if (source < min)
		{
			return min;
		}
		if (source > max)
		{
			return max;
		}
		return source;
	}

	public ColorPickerDialog(Context context, OnColorChangedListener listener, int initialColor) {
		super(context);

		m_listener = listener;
		m_initialColor = initialColor;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OnColorChangedListener l = new OnColorChangedListener() {
			public void colorChanged(int color) {
				m_listener.colorChanged(color);
				dismiss();
			}
		};

		ColorPickerView colorPicker = new ColorPickerView(getContext(), l, m_initialColor);
		setContentView(colorPicker);
		LayoutParams params = colorPicker.getLayoutParams();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = (int) Misc.dipsToPixels(getContext().getResources(), 200);
		colorPicker.setLayoutParams(params);
		setTitle("Pick a color");
	}
}

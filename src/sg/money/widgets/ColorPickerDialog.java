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

	private OnColorChangedListener mListener;
	private int mInitialColor;

	private static class ColorPickerView extends View {
		private Paint mPaint;
		private Paint mPaintBlackWhite;
		private Paint oldPaint;
		private Paint newPaint;
		private Paint highlightPaint;
		private final int[] mColors;
		private final int[] mColorsBlackWhite;
		private OnColorChangedListener mListener;
		private Rect gradientRect;
		private Rect oldRect;
		private Rect newRect;

		ColorPickerView(Context c, OnColorChangedListener l, int color) {
			super(c);
			mListener = l;
			mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
					0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };

			mColorsBlackWhite = new int[] { 0xFF000000, 0x00AAAAAA, 0xFFFFFFFF };

			Shader s = new LinearGradient(0, 0, 0, 0, mColors, null,
					Shader.TileMode.CLAMP);

			Shader sBlackWhite = new LinearGradient(0, 0, 0, 0, mColorsBlackWhite, null,
					Shader.TileMode.CLAMP);

			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setShader(s);
			mPaint.setStyle(Paint.Style.FILL);

			mPaintBlackWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintBlackWhite.setShader(sBlackWhite);
			mPaintBlackWhite.setStyle(Paint.Style.FILL);

			oldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			oldPaint.setColor(color);
			oldPaint.setStyle(Paint.Style.FILL);

			newPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			newPaint.setColor(color);
			newPaint.setStyle(Paint.Style.FILL);

			highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			highlightPaint.setColor(Color.WHITE);
			highlightPaint.setStyle(Paint.Style.STROKE);
			highlightPaint.setStrokeWidth(5);
		}

		private boolean m_onOld, m_onNew, m_onSelector;
		private boolean m_highlightOld, m_highlightNew;

		@Override
		protected void onDraw(Canvas canvas) {

			canvas.drawRect(gradientRect, mPaint);
			canvas.drawRect(gradientRect, mPaintBlackWhite);

			canvas.drawRect(oldRect, oldPaint);
			canvas.drawRect(newRect, newPaint);

			if (m_highlightOld)
				canvas.drawRect(oldRect, highlightPaint);
			if (m_highlightNew)
				canvas.drawRect(newRect, highlightPaint);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			w = this.getWidth();
			int fiveDP = (int) Misc
					.dipsToPixels(getContext().getResources(), 5);
			int tenDP = (int) Misc
					.dipsToPixels(getContext().getResources(), 10);
			int fiftyDP = (int) Misc.dipsToPixels(getContext().getResources(),
					50);
			gradientRect = new Rect(tenDP, tenDP, w - tenDP, h - fiftyDP
					- tenDP);

			oldRect = new Rect(tenDP, h - fiftyDP, (gradientRect.width() / 2)
					+ fiveDP, h - tenDP);
			newRect = new Rect(oldRect.right + tenDP, h - fiftyDP,
					gradientRect.right, h - tenDP);

			Shader s = new LinearGradient(gradientRect.left, gradientRect.top,
					gradientRect.right, gradientRect.top, mColors, null,
					Shader.TileMode.CLAMP);
			mPaint.setShader(s);

			Shader sBlackWhite = new LinearGradient(gradientRect.left, gradientRect.top,
					gradientRect.left, gradientRect.bottom, mColorsBlackWhite, null,
					Shader.TileMode.CLAMP);
			mPaintBlackWhite.setShader(sBlackWhite);
		};

		private static int ave(int s, int d, float p) {
			return s + java.lang.Math.round(p * (d - s));
		}

		private static int interpColor(int colors[], float unit) {
			if (unit <= 0) {
				return colors[0];
			}
			if (unit >= 1) {
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			int i = (int) p;
			p -= i;

			// now p is just the fractional part [0...1) and i is the index
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
			boolean onSelector = gradientRect.contains((int) x, (int) y);
			boolean onOld = oldRect.contains((int) x, (int) y);
			boolean onNew = newRect.contains((int) x, (int) y);

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
					x -= gradientRect.left;
					x = (x / (float) gradientRect.width());
					y -= gradientRect.top;
					y = (y / (float) gradientRect.height());
					int new1 = interpColor(mColors, x);
					int addColor = (int)((510f * y) - 255f);
					newPaint.setColor(Color.argb(
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
						mListener.colorChanged(newPaint.getColor());
					}
				} else if (m_onOld) {
					if (onOld) {
						mListener.colorChanged(oldPaint.getColor());
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
			return min;
		if (source > max)
			return max;
		return source;
	}

	public ColorPickerDialog(Context context, OnColorChangedListener listener,
			int initialColor) {
		super(context);

		mListener = listener;
		mInitialColor = initialColor;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OnColorChangedListener l = new OnColorChangedListener() {
			public void colorChanged(int color) {
				mListener.colorChanged(color);
				dismiss();
			}
		};

		ColorPickerView colorPicker = new ColorPickerView(getContext(), l,
				mInitialColor);
		setContentView(colorPicker);
		LayoutParams params = colorPicker.getLayoutParams();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = (int) Misc.dipsToPixels(getContext().getResources(), 200);
		colorPicker.setLayoutParams(params);
		setTitle("Pick a color");
	}
}
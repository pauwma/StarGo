package com.example.mp08_firebase;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CharacterCountCircleView extends View {
    private static final int MAX_COUNT = 256;

    private Paint circlePaint;
    private Paint fillPaint;
    private RectF rect;

    private int count = 0;

    public CharacterCountCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(getContext().getResources().getColor(R.color.border2));
        circlePaint.setStrokeWidth(8f);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.STROKE);
        fillPaint.setColor(getContext().getResources().getColor(R.color.morado));
        fillPaint.setStrokeWidth(8f);
        fillPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setCharacterCount(int count) {
        this.count = count;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Considera un poco de margen para el grosor del trazo
        float margin = circlePaint.getStrokeWidth() / 2;
        rect = new RectF(margin, margin, w - margin, h - margin);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawOval(rect, circlePaint);
        canvas.drawArc(rect, -90, 360 * count / (float) MAX_COUNT, false, fillPaint);
    }
}
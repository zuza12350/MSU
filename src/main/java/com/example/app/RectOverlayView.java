package com.example.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class RectOverlayView extends View {

    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect;

    public RectOverlayView(Context c, @Nullable AttributeSet a) {
        super(c, a);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(4f);
        stroke.setColor(0xFF32A891);
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(0x3332A891);
    }

    public void setRect(@Nullable RectF r) {
        this.rect = r;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rect != null) {
            canvas.drawRect(rect, fill);
            canvas.drawRect(rect, stroke);
        }
    }
}
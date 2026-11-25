package com.idleminer.clone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EffectsView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<FloatingText> texts = new ArrayList<>();

    public EffectsView(Context context) {
        super(context);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
    }

    public void addFloatingText(String text, float x, float y, int color) {
        texts.add(new FloatingText(text, x, y, color));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Iterator<FloatingText> it = texts.iterator();
        while (it.hasNext()) {
            FloatingText ft = it.next();
            ft.y -= 4f;
            ft.life -= 0.02f;
            if (ft.life <= 0) {
                it.remove();
                continue;
            }
            paint.setColor(ft.color);
            paint.setAlpha((int) (255 * ft.life));
            paint.setTextSize(40);
            canvas.drawText(ft.text, ft.x, ft.y, paint);
        }
        if (!texts.isEmpty()) postInvalidateOnAnimation();
    }

    private static class FloatingText {
        String text; float x, y; int color; float life = 1f;
        FloatingText(String t, float x, float y, int c) {
            this.text = t; this.x = x; this.y = y; this.color = c;
        }
    }
}

package com.idleminer.clone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MineView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Mine mine;
    private int mineIndex;
    private GameManager game;
    private float animationOffset = 0;
    private Bitmap minerBitmap;
    private Bitmap bgBitmap;

    public MineView(Context context, Mine mine, int mineIndex) {
        super(context);
        this.mine = mine;
        this.mineIndex = mineIndex;
        this.game = GameManager.getInstance(context);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);

        // تم إزالة تحميل صورة العامل مؤقتاً لأنها غير موجودة
        try {
            bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        } catch (Exception e) {}
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (bgBitmap != null) {
            Rect destRect = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(bgBitmap, null, destRect, paint);
        } else {
            paint.setColor(Color.parseColor("#1A1A2E"));
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
        
        int startY = 100;
        int floorHeight = 350;
        
        for (int i = 0; i < mine.miners.size(); i++) {
            Miner m = mine.miners.get(i);
            float y = startY + (i * floorHeight);
            
            paint.setColor(Color.parseColor("#3E2723"));
            canvas.drawRect(0, y + 280, getWidth(), y + 320, paint);
            
            drawMiner(canvas, getWidth() / 2f, y + 160, m.level > 0);
            
            textPaint.setTextSize(42);
            textPaint.setColor(Color.WHITE);
            canvas.drawText(m.name + " (مستوى " + m.level + ")", getWidth() / 2f, y + 50, textPaint);
            
            textPaint.setTextSize(30);
            textPaint.setColor(Color.parseColor("#4CAF50"));
            canvas.drawText("+" + game.formatNumber(m.getTotalProduction()) + " / ثانية", getWidth() / 2f, y + 100, textPaint);
            
            drawUpgradeButton(canvas, getWidth() / 2f, y + 250, m);
        }

        if (game.managers.get(mineIndex)) {
            animationOffset += 0.1f;
            postInvalidateDelayed(50);
        }
    }

    private void drawMiner(Canvas canvas, float x, float y, boolean isActive) {
        // رسم دائرة كاحتياط حتى نضع صورة العامل
        float offset = (float) Math.sin(animationOffset) * 10f;
        if (!game.managers.get(mineIndex)) offset = 0;
        
        paint.setColor(isActive ? Color.parseColor("#FFD700") : Color.GRAY);
        canvas.drawRoundRect(x - 50 + offset, y - 40, x + 50 + offset, y + 70, 20, 20, paint);
        paint.setColor(Color.parseColor("#FFCC80"));
        canvas.drawCircle(x + offset, y - 90, 45, paint);
    }

    private void drawUpgradeButton(Canvas canvas, float x, float y, Miner m) {
        paint.setColor(Color.parseColor("#2196F3"));
        RectF btn = new RectF(x - 250, y - 50, x + 250, y + 50);
        canvas.drawRoundRect(btn, 25, 25, paint);
        
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(35);
        canvas.drawText("ترقية - " + game.formatNumber(m.currentCost) + " 💰", x, y + 12, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float y = event.getY();
            int startY = 100;
            int floorHeight = 350;
            
            for (int i = 0; i < mine.miners.size(); i++) {
                float btnY = startY + (i * floorHeight) + 250;
                if (y > btnY - 50 && y < btnY + 50) {
                    if (game.upgradeMiner(mineIndex, i)) {
                        invalidate();
                    } else {
                        Toast.makeText(getContext(), "تحتاج المزيد من الذهب!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}

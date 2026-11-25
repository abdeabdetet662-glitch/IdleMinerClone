package com.idleminer.clone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MineView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Mine mine;
    private int mineIndex;
    private GameManager game;
    private float animationOffset = 0; // لتحريك العمال

    public MineView(Context context, Mine mine, int mineIndex) {
        super(context);
        this.mine = mine;
        this.mineIndex = mineIndex;
        this.game = GameManager.getInstance(context);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 1. رسم خلفية المنجم (جبال وصخور)
        drawBackground(canvas);
        
        int startY = 100;
        int floorHeight = 350; // زيادة المساحة قليلاً
        
        for (int i = 0; i < mine.miners.size(); i++) {
            Miner m = mine.miners.get(i);
            float y = startY + (i * floorHeight);
            
            // رسم أرضية الطابق
            paint.setColor(Color.parseColor("#2C2C2C"));
            canvas.drawRect(0, y + 280, getWidth(), y + 320, paint);
            
            // العامل
            drawMiner(canvas, getWidth() / 2f, y + 160, m.level > 0);
            
            // الاسم والمستوى
            paint.setColor(Color.WHITE);
            paint.setTextSize(42);
            canvas.drawText(m.name + " (مستوى " + m.level + ")", getWidth() / 2f, y + 50, paint);
            
            // الإنتاج
            paint.setColor(Color.parseColor("#4CAF50"));
            paint.setTextSize(30);
            canvas.drawText("+" + game.formatNumber(m.getTotalProduction()) + " / ثانية", getWidth() / 2f, y + 100, paint);
            
            // زر الترقية
            drawUpgradeButton(canvas, getWidth() / 2f, y + 250, m);
        }

        // تحديث الرسوم المتحركة (اهتزاز العمال إذا كان هناك مدير)
        if (game.managers.get(mineIndex)) {
            animationOffset += 0.1f;
            postInvalidateDelayed(50); // إعادة الرسم بسرعة
        }
    }

    private void drawBackground(Canvas canvas) {
        // تدرج لوني من الأزرق الداكن للأسود
        paint.setColor(Color.parseColor("#1A1A2E"));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        
        // رسم بعض الجبال
        paint.setColor(Color.parseColor("#16213E"));
        Path path = new Path();
        path.moveTo(0, getHeight());
        path.lineTo(getWidth() * 0.3f, getHeight() * 0.5f);
        path.lineTo(getWidth() * 0.6f, getHeight() * 0.8f);
        path.lineTo(getWidth(), getHeight() * 0.4f);
        path.lineTo(getWidth(), getHeight());
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawMiner(Canvas canvas, float x, float y, boolean isActive) {
        // اهتزاز العامل إذا كان المدير موجوداً
        float offset = (float) Math.sin(animationOffset) * 10f;
        if (!game.managers.get(mineIndex)) offset = 0;

        // الجسم
        paint.setColor(isActive ? Color.parseColor("#FFD700") : Color.GRAY);
        canvas.drawRoundRect(x - 50 + offset, y - 40, x + 50 + offset, y + 70, 20, 20, paint);
        
        // الرأس
        paint.setColor(Color.parseColor("#FFCC80"));
        canvas.drawCircle(x + offset, y - 90, 45, paint);
        
        // القبعة
        paint.setColor(Color.parseColor("#FF5722"));
        canvas.drawRect(x - 45 + offset, y - 140, x + 45 + offset, y - 100, paint);
        canvas.drawRect(x - 55 + offset, y - 105, x + 55 + offset, y - 85, paint);
    }

    private void drawUpgradeButton(Canvas canvas, float x, float y, Miner m) {
        paint.setColor(Color.parseColor("#2196F3"));
        RectF btn = new RectF(x - 250, y - 50, x + 250, y + 50);
        canvas.drawRoundRect(btn, 25, 25, paint);
        
        paint.setColor(Color.WHITE);
        paint.setTextSize(35);
        canvas.drawText("ترقية - " + game.formatNumber(m.currentCost) + " 💰", x, y + 12, paint);
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

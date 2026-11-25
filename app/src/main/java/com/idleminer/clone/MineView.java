package com.idleminer.clone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MineView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Mine mine;
    private int mineIndex;
    private GameManager game;

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
        
        canvas.drawColor(Color.parseColor("#1A1A1A"));
        
        int startY = 100;
        int floorHeight = 300;
        
        for (int i = 0; i < mine.miners.size(); i++) {
            Miner m = mine.miners.get(i);
            float y = startY + (i * floorHeight);
            
            // الأرضية
            paint.setColor(Color.parseColor("#2C2C2C"));
            canvas.drawRect(0, y + 250, getWidth(), y + 280, paint);
            
            // العامل
            drawMiner(canvas, getWidth() / 2f, y + 150, m.level > 0);
            
            // الاسم
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            canvas.drawText(m.name + " (مستوى " + m.level + ")", getWidth() / 2f, y + 50, paint);
            
            // الإنتاج
            paint.setColor(Color.parseColor("#4CAF50"));
            paint.setTextSize(28);
            canvas.drawText("+" + game.formatNumber(m.getTotalProduction()) + " / ثانية", getWidth() / 2f, y + 95, paint);
            
            // زر الترقية
            drawUpgradeButton(canvas, getWidth() / 2f, y + 220, m);
        }
    }

    private void drawMiner(Canvas canvas, float x, float y, boolean isActive) {
        paint.setColor(isActive ? Color.parseColor("#FFD700") : Color.GRAY);
        canvas.drawRoundRect(x - 40, y - 30, x + 40, y + 50, 15, 15, paint);
        
        paint.setColor(Color.parseColor("#FFCC80"));
        canvas.drawCircle(x, y - 70, 35, paint);
        
        paint.setColor(Color.parseColor("#FF5722"));
        canvas.drawRect(x - 35, y - 110, x + 35, y - 80, paint);
    }

    private void drawUpgradeButton(Canvas canvas, float x, float y, Miner m) {
        paint.setColor(Color.parseColor("#2196F3"));
        RectF btn = new RectF(x - 200, y - 40, x + 200, y + 40);
        canvas.drawRoundRect(btn, 20, 20, paint);
        
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        canvas.drawText("ترقية - " + game.formatNumber(m.currentCost) + " 💰", x, y + 10, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float y = event.getY();
            int startY = 100;
            int floorHeight = 300;
            
            for (int i = 0; i < mine.miners.size(); i++) {
                float btnY = startY + (i * floorHeight) + 220;
                if (y > btnY - 40 && y < btnY + 40) {
                    if (game.upgradeMiner(mineIndex, i)) {
                        invalidate(); // إعادة رسم الشاشة لتحديث الأرقام
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

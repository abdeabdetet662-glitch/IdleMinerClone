package com.idleminer.clone;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private GameManager game;
    private TextView goldText;
    private LinearLayout contentLayout;
    private LinearLayout tabsLayout;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable loop;
    private int currentMineIndex = 0;
    private EffectsView effects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = GameManager.getInstance(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#121212"));

        // === 1. الشريط العلوي (الذهب) ===
        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER);
        topBar.setPadding(0, 60, 0, 30);
        topBar.setBackgroundColor(Color.parseColor("#1E1E1E"));
        
        goldText = new TextView(this);
        goldText.setTextColor(Color.parseColor("#FFD700"));
        goldText.setTextSize(32);
        goldText.setTypeface(null, Typeface.BOLD);
        topBar.addView(goldText);
        root.addView(topBar);

        // === 2. منطقة المحتوى (المنجم الحالي) ===
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(40, 40, 40, 40);
        scroll.addView(contentLayout);
        root.addView(scroll);

        // === 3. شريط التنقل السفلي (المناجم) ===
        tabsLayout = new LinearLayout(this);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabsLayout.setBackgroundColor(Color.parseColor("#1E1E1E"));
        tabsLayout.setPadding(20, 20, 20, 40);
        root.addView(tabsLayout);

        // === 4. إضافة المؤثرات البصرية (الرقم الطائر) ===
        FrameLayout mainFrame = new FrameLayout(this);
        mainFrame.addView(root);
        effects = new EffectsView(this);
        mainFrame.addView(effects, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        
        setContentView(mainFrame);

        buildTabs();
        loadMineContent(currentMineIndex);
        startGameLoop();
    }

    private void buildTabs() {
        tabsLayout.removeAllViews();
        for (int i = 0; i < game.mines.size(); i++) {
            final int index = i;
            Mine m = game.mines.get(i);
            
            Button tabBtn = new Button(this);
            tabBtn.setText(m.name);
            tabBtn.setTextSize(14);
            tabBtn.setTypeface(null, Typeface.BOLD);
            tabBtn.setTextColor(m.isUnlocked ? Color.WHITE : Color.GRAY);
            
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(20);
            if (index == currentMineIndex) {
                bg.setColor(Color.parseColor("#FFD700"));
                tabBtn.setTextColor(Color.BLACK);
            } else {
                bg.setColor(Color.parseColor("#2C2C2C"));
            }
            tabBtn.setBackground(bg);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(10, 0, 10, 0);
            tabBtn.setLayoutParams(params);

            tabBtn.setOnClickListener(v -> {
                currentMineIndex = index;
                buildTabs();
                loadMineContent(currentMineIndex);
            });

            tabsLayout.addView(tabBtn);
        }
    }

    private void loadMineContent(int mineIndex) {
        contentLayout.removeAllViews();
        Mine mine = game.mines.get(mineIndex);

        // عنوان المنجم
        TextView title = new TextView(this);
        title.setText("⛏️ " + mine.name);
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        contentLayout.addView(title);

        if (!mine.isUnlocked) {
            // شاشة قفل المنجم
            TextView lockMsg = new TextView(this);
            lockMsg.setText("🔒 هذا المنجم مقفل\nتحتاج " + game.formatNumber(mine.unlockCost) + " لفتحه");
            lockMsg.setTextColor(Color.parseColor("#AAAAAA"));
            lockMsg.setTextSize(18);
            lockMsg.setGravity(Gravity.CENTER);
            lockMsg.setPadding(0, 100, 0, 50);
            contentLayout.addView(lockMsg);

            Button unlockBtn = new Button(this);
            unlockBtn.setText("فتح المنجم");
            unlockBtn.setTextColor(Color.BLACK);
            GradientDrawable btnBg = new GradientDrawable();
            btnBg.setColor(Color.parseColor("#4CAF50"));
            btnBg.setCornerRadius(30);
            unlockBtn.setBackground(btnBg);
            unlockBtn.setOnClickListener(v -> {
                if (game.unlockMine(mineIndex)) {
                    buildTabs();
                    loadMineContent(mineIndex);
                } else {
                    Toast.makeText(this, "لا تملك ذهباً كافياً!", Toast.LENGTH_SHORT).show();
                }
            });
            contentLayout.addView(unlockBtn);
            return;
        }

        // معلومات الدخل
        TextView incomeInfo = new TextView(this);
        incomeInfo.setText("الإنتاج الحالي: +" + game.formatNumber(mine.getTotalIncome()) + " / ثانية");
        incomeInfo.setTextColor(Color.parseColor("#4CAF50"));
        incomeInfo.setTextSize(16);
        incomeInfo.setGravity(Gravity.CENTER);
        incomeInfo.setPadding(0, 20, 0, 40);
        contentLayout.addView(incomeInfo);

        // زر التعدين اليدوي
        Button tapBtn = new Button(this);
        tapBtn.setText("تعدين يدوي 🪨");
        tapBtn.setTextColor(Color.BLACK);
        GradientDrawable tapBg = new GradientDrawable();
        tapBg.setColor(Color.parseColor("#FFD700"));
        tapBg.setCornerRadius(50);
        tapBtn.setBackground(tapBg);
        tapBtn.setOnClickListener(v -> {
            double amount = 1 + (mine.getTotalIncome() * 0.1);
            game.gold += amount; 
            updateGoldUI();
            
            // إضافة المؤثر البصري
            if (effects != null) {
                effects.addFloatingText("+" + game.formatNumber(amount), 
                                        v.getX() + v.getWidth() / 2f, 
                                        v.getY() + v.getHeight() / 2f, 
                                        Color.parseColor("#FFD700"));
            }
            
            // إضافة الصوت
            try {
                android.media.ToneGenerator tone = new android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 80);
                tone.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 40);
            } catch (Exception e) {}
        });
        contentLayout.addView(tapBtn);

        // قائمة العمال
        for (int i = 0; i < mine.miners.size(); i++) {
            final int minerIndex = i;
            Miner worker = mine.miners.get(i);
            
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(30, 20, 30, 20);
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(Color.parseColor("#2C2C2C"));
            cardBg.setCornerRadius(20);
            card.setBackground(cardBg);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 10, 0, 10);
            card.setLayoutParams(params);

            // === رسم العامل الكرتوني ===
            View workerAvatar = new View(this) {
                @Override
                protected void onDraw(Canvas canvas) {
                    super.onDraw(canvas);
                    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                    
                    // الجسم
                    p.setColor(Color.parseColor("#FFD700"));
                    canvas.drawRoundRect(20, 40, 80, 100, 10, 10, p);
                    
                    // الرأس
                    p.setColor(Color.parseColor("#FFCC80"));
                    canvas.drawCircle(50, 30, 25, p);
                    
                    // القبعة
                    p.setColor(Color.parseColor("#FF5722"));
                    canvas.drawRect(25, 5, 75, 25, p);
                    canvas.drawRect(15, 20, 85, 30, p);
                }
            };
            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(120, 120);
            avatarParams.setMargins(0, 0, 20, 0);
            workerAvatar.setLayoutParams(avatarParams);
            card.addView(workerAvatar);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView name = new TextView(this);
            name.setText(worker.name + " (مستوى " + worker.level + ")");
            name.setTextColor(Color.WHITE);
            name.setTextSize(16);
            info.addView(name);

            TextView prod = new TextView(this);
            prod.setText("ينتج: +" + game.formatNumber(worker.getTotalProduction()) + " / ثانية");
            prod.setTextColor(Color.parseColor("#AAAAAA"));
            prod.setTextSize(12);
            info.addView(prod);

            card.addView(info);

            Button upBtn = new Button(this);
            upBtn.setText(game.formatNumber(worker.currentCost) + " 💰");
            upBtn.setTextColor(Color.BLACK);
            GradientDrawable upBg = new GradientDrawable();
            upBg.setColor(Color.parseColor("#2196F3"));
            upBg.setCornerRadius(15);
            upBtn.setBackground(upBg);
            
            upBtn.setOnClickListener(v -> {
                if (game.upgradeMiner(mineIndex, minerIndex)) {
                    loadMineContent(mineIndex); 
                    updateGoldUI();
                    // صوت الترقية
                    try {
                        android.media.ToneGenerator tone = new android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 80);
                        tone.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 80);
                    } catch (Exception e) {}
                } else {
                    Toast.makeText(this, "تحتاج المزيد من الذهب!", Toast.LENGTH_SHORT).show();
                }
            });
            card.addView(upBtn);

            contentLayout.addView(card);
        }
    }

    private void updateGoldUI() {
        goldText.setText("💰 " + game.formatNumber(game.gold));
    }

    private void startGameLoop() {
        loop = new Runnable() {
            @Override
            public void run() {
                for (Mine m : game.mines) {
                    if (m.isUnlocked) {
                        game.gold += m.getTotalIncome() / 10.0;
                    }
                }
                updateGoldUI();
                handler.postDelayed(this, 100);
            }
        };
        handler.post(loop);
    }

    @Override
    protected void onPause() {
        super.onPause();
        game.saveGame();
        handler.removeCallbacks(loop);
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.loadGame();
        updateGoldUI();
        startGameLoop();
    }
}

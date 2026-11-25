package com.idleminer.clone;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
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

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(contentLayout);
        root.addView(scroll);

        tabsLayout = new LinearLayout(this);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabsLayout.setBackgroundColor(Color.parseColor("#1E1E1E"));
        tabsLayout.setPadding(20, 20, 20, 40);
        root.addView(tabsLayout);

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

        if (!mine.isUnlocked) {
            // شاشة القفل
            LinearLayout lockLayout = new LinearLayout(this);
            lockLayout.setOrientation(LinearLayout.VERTICAL);
            lockLayout.setGravity(Gravity.CENTER);
            lockLayout.setPadding(40, 200, 40, 40);
            
            TextView lockMsg = new TextView(this);
            lockMsg.setText("🔒 هذا المنجم مقفل\nتحتاج " + game.formatNumber(mine.unlockCost) + " لفتحه");
            lockMsg.setTextColor(Color.parseColor("#AAAAAA"));
            lockMsg.setTextSize(18);
            lockMsg.setGravity(Gravity.CENTER);
            lockLayout.addView(lockMsg);

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
            lockLayout.addView(unlockBtn);
            contentLayout.addView(lockLayout);
            return;
        }

        // زر شراء المدير (إذا لم يكن موجوداً)
        if (!game.managers.get(mineIndex)) {
            Button managerBtn = new Button(this);
            managerBtn.setText("توظيف مدير (يعمل تلقائياً) - 1000 💰");
            managerBtn.setTextColor(Color.WHITE);
            GradientDrawable mBg = new GradientDrawable();
            mBg.setColor(Color.parseColor("#E91E63"));
            mBg.setCornerRadius(25);
            managerBtn.setBackground(mBg);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(40, 20, 40, 20);
            managerBtn.setLayoutParams(params);
            
            managerBtn.setOnClickListener(v -> {
                if (game.buyManager(mineIndex, 1000)) {
                    loadMineContent(mineIndex);
                    Toast.makeText(this, "تم توظيف المدير! العمال يعملون الآن تلقائياً.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "تحتاج 1000 ذهبة لتوظيف المدير!", Toast.LENGTH_SHORT).show();
                }
            });
            contentLayout.addView(managerBtn);
        }

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
            if (effects != null) {
                effects.addFloatingText("+" + game.formatNumber(amount), 
                                        v.getX() + v.getWidth() / 2f, 
                                        v.getY() + v.getHeight() / 2f, 
                                        Color.parseColor("#FFD700"));
            }
        });
        contentLayout.addView(tapBtn);

        // الواجهة الرسومية للعمال
        MineView mineView = new MineView(this, mine, mineIndex);
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().density * 1800));
        mineView.setLayoutParams(viewParams);
        contentLayout.addView(mineView);
    }

    private void updateGoldUI() {
        goldText.setText("💰 " + game.formatNumber(game.gold));
    }

    private void startGameLoop() {
        loop = new Runnable() {
            @Override
            public void run() {
                boolean needsRedraw = false;
                for (int i = 0; i < game.mines.size(); i++) {
                    Mine m = game.mines.get(i);
                    // إذا كان المنجم مفتوحاً وتم تعيين مدير، ينتج تلقائياً
                    if (m.isUnlocked && game.managers.get(i)) {
                        game.gold += m.getTotalIncome() / 10.0;
                        needsRedraw = true;
                    }
                }
                if (needsRedraw) updateGoldUI();
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

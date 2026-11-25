package com.idleminer.clone;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameManager {
    private static GameManager instance;
    private SharedPreferences prefs;

    public double gold = 0;
    public List<Mine> mines = new ArrayList<>();
    public long lastSavedTime;

    private GameManager(Context context) {
        this.prefs = context.getSharedPreferences("IdleMinerSave", Context.MODE_PRIVATE);
        setupMines();
        loadGame();
    }

    public static GameManager getInstance(Context context) {
        if (instance == null) instance = new GameManager(context.getApplicationContext());
        return instance;
    }

    private void setupMines() {
        mines.add(new Mine(0, "منجم الفحم", "فحم", 0));
        mines.add(new Mine(1, "منجم الذهب", "ذهب", 5000));
        mines.add(new Mine(2, "منجم الألماس", "ألماس", 100000));
    }

    public void calculateOfflineIncome() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastSavedTime;
        if (timeDiff > 0) {
            double seconds = timeDiff / 1000.0;
            for (Mine m : mines) {
                if (m.isUnlocked) {
                    gold += m.getTotalIncome() * seconds;
                }
            }
        }
        lastSavedTime = currentTime;
    }

    public boolean upgradeMiner(int mineIndex, int minerIndex) {
        Mine m = mines.get(mineIndex);
        Miner worker = m.miners.get(minerIndex);
        
        if (gold >= worker.currentCost) {
            gold -= worker.currentCost;
            worker.level++;
            worker.currentCost = (long) (worker.baseCost * Math.pow(1.15, worker.level));
            
            if (worker.level % 25 == 0) {
                worker.baseIncome *= 2; 
            }
            saveGame();
            return true;
        }
        return false;
    }

    public boolean unlockMine(int index) {
        Mine m = mines.get(index);
        if (!m.isUnlocked && gold >= m.unlockCost) {
            gold -= m.unlockCost;
            m.isUnlocked = true;
            saveGame();
            return true;
        }
        return false;
    }

    public String formatNumber(double number) {
        if (number < 1000) return String.format(Locale.US, "%.1f", number);
        int exp = (int) (Math.log(number) / Math.log(1000));
        char suffix = "KMBTQ".charAt(Math.min(exp - 1, 4));
        return String.format(Locale.US, "%.1f%c", number / Math.pow(1000, exp), suffix);
    }

    public void saveGame() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("gold", (float) gold);
        editor.putLong("lastSavedTime", System.currentTimeMillis());
        for (int i = 0; i < mines.size(); i++) {
            Mine m = mines.get(i);
            editor.putBoolean("unlocked_" + i, m.isUnlocked);
            for (int j = 0; j < m.miners.size(); j++) {
                Miner worker = m.miners.get(j);
                editor.putInt("lvl_" + i + "_" + j, worker.level);
                editor.putLong("cost_" + i + "_" + j, worker.currentCost);
            }
        }
        editor.apply();
    }

    public void loadGame() {
        gold = prefs.getFloat("gold", 0f);
        lastSavedTime = prefs.getLong("lastSavedTime", System.currentTimeMillis());
        for (int i = 0; i < mines.size(); i++) {
            Mine m = mines.get(i);
            m.isUnlocked = prefs.getBoolean("unlocked_" + i, i == 0);
            for (int j = 0; j < m.miners.size(); j++) {
                Miner worker = m.miners.get(j);
                worker.level = prefs.getInt("lvl_" + i + "_" + j, 0);
                worker.currentCost = prefs.getLong("cost_" + i + "_" + j, worker.baseCost);
            }
        }
        calculateOfflineIncome();
    }
}

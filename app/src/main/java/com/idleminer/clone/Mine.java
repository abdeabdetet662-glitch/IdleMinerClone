package com.idleminer.clone;

import java.util.ArrayList;
import java.util.List;

public class Mine {
    public int id;
    public String name;
    public String currencyName;
    public long unlockCost;
    public boolean isUnlocked;
    public List<Miner> miners = new ArrayList<>();

    public Mine(int id, String name, String currencyName, long unlockCost) {
        this.id = id;
        this.name = name;
        this.currencyName = currencyName;
        this.unlockCost = unlockCost;
        this.isUnlocked = (id == 0);
        setupMiners();
    }

    private void setupMiners() {
        miners.add(new Miner("عامل مبتدئ", 10, 1.0));
        miners.add(new Miner("عامل محترف", 100, 10.0));
        miners.add(new Miner("مشرف", 1000, 100.0));
        miners.add(new Miner("مهندس", 10000, 1000.0));
        miners.add(new Miner("آلة حفر", 100000, 10000.0));
    }

    public double getTotalIncome() {
        double total = 0;
        for (Miner m : miners) total += m.getTotalProduction();
        return total;
    }
}

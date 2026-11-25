package com.idleminer.clone;

public class Miner {
    public String name;
    public int level = 0;
    public long baseCost;
    public long currentCost;
    public double baseIncome;

    public Miner(String name, long baseCost, double baseIncome) {
        this.name = name;
        this.baseCost = baseCost;
        this.currentCost = baseCost;
        this.baseIncome = baseIncome;
    }

    public double getTotalProduction() {
        return level * baseIncome;
    }
}

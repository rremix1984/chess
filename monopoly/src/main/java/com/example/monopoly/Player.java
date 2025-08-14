package com.example.monopoly;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * 大富翁游戏玩家类
 */
public class Player {
    private String name;
    private Color color;
    private int money;
    private int position;
    private List<String> properties;
    private boolean inJail;
    private int jailTurns;
    
    public Player(String name, Color color, int initialMoney) {
        this.name = name;
        this.color = color;
        this.money = initialMoney;
        this.position = 0; // 起点
        this.properties = new ArrayList<>();
        this.inJail = false;
        this.jailTurns = 0;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public int getMoney() {
        return money;
    }
    
    public void setMoney(int money) {
        this.money = money;
    }
    
    public void addMoney(int amount) {
        this.money += amount;
    }
    
    public boolean subtractMoney(int amount) {
        if (this.money >= amount) {
            this.money -= amount;
            return true;
        }
        return false;
    }
    
    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public List<String> getProperties() {
        return new ArrayList<>(properties);
    }
    
    public void addProperty(String property) {
        properties.add(property);
    }
    
    public void removeProperty(String property) {
        properties.remove(property);
    }
    
    public boolean hasProperty(String property) {
        return properties.contains(property);
    }
    
    public boolean isInJail() {
        return inJail;
    }
    
    public void setInJail(boolean inJail) {
        this.inJail = inJail;
        if (!inJail) {
            this.jailTurns = 0;
        }
    }
    
    public int getJailTurns() {
        return jailTurns;
    }
    
    public void incrementJailTurns() {
        this.jailTurns++;
    }
    
    public boolean isBankrupt() {
        return money <= 0;
    }
    
    @Override
    public String toString() {
        return String.format("%s (位置: %d, 现金: $%d, 资产: %d)", 
                           name, position, money, properties.size());
    }
}

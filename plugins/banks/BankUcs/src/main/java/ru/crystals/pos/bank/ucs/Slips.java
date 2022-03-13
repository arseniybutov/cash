package ru.crystals.pos.bank.ucs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Slips {
    private List<List<String>> slips = new ArrayList<>();
    private boolean isFull = true;

    public boolean areFull() {
        return isFull;
    }

    public List<List<String>> getSlips() {
        return slips;
    }

    public void addSlips(Collection<List<String>> newSlips) {
        slips.addAll(newSlips);
    }

    public void setFull(boolean isFull) {
        this.isFull = isFull;
    }

    public List<String> get(int i) {
        return slips.get(i);
    }

    public int size() {
        return slips.size();
    }
}

package com.example.flightchess.core;

import java.util.HashSet;
import java.util.Set;

public class Rules {
    public boolean allowStack = true;
    public boolean enableSafeCells = false;
    public boolean chainJump = true;
    public boolean extraRollOnSix = true;
    public int maxExtraRollChain = 3;
    public Set<Integer> safeCells = new HashSet<>();
}

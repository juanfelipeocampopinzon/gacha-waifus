package com.gachawaifus.entity;

public final class WildWaifuConfig {
    private WildWaifuConfig() {
    }

    public static boolean active() {
        return true;
    }

    public static int chance() {
        return 120;
    }

    public static int interval() {
        return 400;
    }

    public static int maxNearby() {
        return 2;
    }

    public static int claimCost() {
        return 1;
    }

    public static int tameOdds() {
        return 110;
    }

    public static int tamePity() {
        return 128;
    }
}

package com.gachawaifus.entity;

public final class RecallConfig {
    private RecallConfig() {
    }

    public static boolean enabled() {
        return true;
    }

    public static boolean crossDimension() {
        return true;
    }

    public static int distance() {
        return 96;
    }
}

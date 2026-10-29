package com.gachawaifus.entity;

public final class WanderConfig {
    public static final int MAX_RANGE = 64;
    private static final int DEFAULT_RANGE = 16;

    private WanderConfig() {
    }

    public static int range() {
        return DEFAULT_RANGE;
    }
}

package com.gachawaifus.team;

import java.util.function.Supplier;

public final class TeamConfig {
    public static final Spec SPEC = new Spec();
    public static final Supplier<Integer> LIMIT = () -> 0;

    public static class Spec {
        public boolean isLoaded() {
            return true;
        }
    }

    private TeamConfig() {
    }
}

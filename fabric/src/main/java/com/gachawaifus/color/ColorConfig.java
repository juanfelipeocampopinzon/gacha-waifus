package com.gachawaifus.color;

import java.util.function.Supplier;

public final class ColorConfig {
    public static final Supplier<Boolean> FLOATING_LABEL = () -> true;
    public static final Supplier<Integer> LABEL_RANGE = () -> 64;
    public static final Supplier<Boolean> WAIFU_NAME_TAGS = () -> true;

    private ColorConfig() {
    }
}

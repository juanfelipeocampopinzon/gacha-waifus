package com.gachawaifus.gacha;

import java.util.List;
import java.util.Set;

public final class GachaConfig {
    private static final List<String> DEFAULT_IDS = List.of(
            "von_lycaon", "billy_kid", "manato", "harumasa", "hugo_vlad", "pan_yinhu", "anton",
            "ben_bigger", "seth_lowell", "lighter", "banyue", "pyrois");

    private static final Set<String> IDS = Set.copyOf(DEFAULT_IDS);

    private GachaConfig() {
    }

    public static boolean excluded() {
        return false;
    }

    public static Set<String> ids() {
        return IDS;
    }
}

package com.wms.outbound.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 波次状态机：PENDING → GENERATING → GENERATED / CANCELLED。
 */
public final class WaveStatus {

    public static final String PENDING = "PENDING";
    public static final String GENERATING = "GENERATING";
    public static final String GENERATED = "GENERATED";
    public static final String CANCELLED = "CANCELLED";

    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<>();

    static {
        TRANSITIONS.put(PENDING, Set.of(GENERATING, CANCELLED));
        TRANSITIONS.put(GENERATING, Set.of(GENERATED, CANCELLED));
        TRANSITIONS.put(GENERATED, Set.of(CANCELLED));
        TRANSITIONS.put(CANCELLED, Collections.emptySet());
    }

    private WaveStatus() {
    }

    public static boolean canTransition(String from, String to) {
        return TRANSITIONS.getOrDefault(from, Collections.emptySet()).contains(to);
    }
}

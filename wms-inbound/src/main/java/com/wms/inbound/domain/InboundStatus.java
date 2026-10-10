package com.wms.inbound.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 入库单状态机：定义合法状态流转。
 *
 * <pre>
 * DRAFT ──确认──▶ CONFIRMED ──到货──▶ ARRIVED ──提交质检──▶ PENDING_QC
 *                                                   │
 *   COMPLETED ◀──完成── PUTTING_AWAY ◀──────────────┘ (质检通过)
 *   任意状态 ──取消──▶ CANCELLED（终态）
 * </pre>
 */
public final class InboundStatus {

    public static final String DRAFT = "DRAFT";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String ARRIVED = "ARRIVED";
    public static final String PENDING_QC = "PENDING_QC";
    public static final String PUTTING_AWAY = "PUTTING_AWAY";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private static final Set<String> TERMINAL = Set.of(COMPLETED, CANCELLED);

    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<>();

    static {
        TRANSITIONS.put(DRAFT, Set.of(CONFIRMED, CANCELLED));
        TRANSITIONS.put(CONFIRMED, Set.of(ARRIVED, CANCELLED));
        TRANSITIONS.put(ARRIVED, Set.of(PENDING_QC, CANCELLED));
        TRANSITIONS.put(PENDING_QC, Set.of(PUTTING_AWAY, CANCELLED));
        TRANSITIONS.put(PUTTING_AWAY, Set.of(COMPLETED, CANCELLED));
        TRANSITIONS.put(COMPLETED, Collections.emptySet());
        TRANSITIONS.put(CANCELLED, Collections.emptySet());
    }

    private InboundStatus() {
    }

    /**
     * 是否允许从 from 流转到 to。
     */
    public static boolean canTransition(String from, String to) {
        return TRANSITIONS.getOrDefault(from, Collections.emptySet()).contains(to);
    }

    /**
     * 是否为终态（不可再流转）。
     */
    public static boolean isTerminal(String status) {
        return TERMINAL.contains(status);
    }
}

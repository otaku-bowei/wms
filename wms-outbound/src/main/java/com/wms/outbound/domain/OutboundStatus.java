package com.wms.outbound.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 出库单状态机：定义合法状态流转。
 *
 * <pre>
 * DRAFT ──分配库存──▶ ALLOCATED ──生成拣货任务──▶ PICKING ──拣货完成──▶ PICKED
 *                                                            │
 *   CANCELLED（终态）◀──任意状态──┘                      PACKING ──打包完成──▶ SHIPPED
 * </pre>
 */
public final class OutboundStatus {

    public static final String DRAFT = "DRAFT";
    public static final String ALLOCATED = "ALLOCATED";
    public static final String PICKING = "PICKING";
    public static final String PICKED = "PICKED";
    public static final String PACKING = "PACKING";
    public static final String SHIPPED = "SHIPPED";
    public static final String CANCELLED = "CANCELLED";

    private static final Set<String> TERMINAL = Set.of(SHIPPED, CANCELLED);

    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<>();

    static {
        TRANSITIONS.put(DRAFT, Set.of(ALLOCATED, CANCELLED));
        TRANSITIONS.put(ALLOCATED, Set.of(PICKING, CANCELLED));
        TRANSITIONS.put(PICKING, Set.of(PICKED, CANCELLED));
        TRANSITIONS.put(PICKED, Set.of(PACKING, CANCELLED));
        TRANSITIONS.put(PACKING, Set.of(SHIPPED, CANCELLED));
        TRANSITIONS.put(SHIPPED, Collections.emptySet());
        TRANSITIONS.put(CANCELLED, Collections.emptySet());
    }

    private OutboundStatus() {
    }

    public static boolean canTransition(String from, String to) {
        return TRANSITIONS.getOrDefault(from, Collections.emptySet()).contains(to);
    }

    public static boolean isTerminal(String status) {
        return TERMINAL.contains(status);
    }
}

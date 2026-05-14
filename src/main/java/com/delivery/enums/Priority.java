package com.delivery.enums;

/**
 * Priority level for delivery orders.
 * NORMAL: Standard delivery priority (default)
 * URGENT: High-priority delivery requiring expedited handling
 */
public enum Priority {
    NORMAL(1),
    URGENT(2);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}

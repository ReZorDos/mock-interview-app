package com.technokratos.agona.enums;

public enum ScheduleType {
    FULL_TIME("Полный день"),
    PART_TIME("Неполный день"),
    REMOTE("Удалённо"),
    HYBRID("Гибридный"),
    SHIFT("Сменный");

    private final String displayName;

    ScheduleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

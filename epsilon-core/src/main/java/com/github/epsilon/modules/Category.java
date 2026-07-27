package com.github.epsilon.modules;

/**
 * 模块分类枚举（epsilon-core 简化版本）
 * 不依赖图形层和 i18n 实现，仅提供 ID。
 * UI 层负责获取图标和翻译。
 */
public enum Category {
    COMBAT("combat"),
    PLAYER("player"),
    MOVEMENT("movement"),
    RENDER("render");

    private final String id;

    Category(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}

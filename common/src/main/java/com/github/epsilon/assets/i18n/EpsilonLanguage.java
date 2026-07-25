package com.github.epsilon.assets.i18n;

public enum EpsilonLanguage {
    Auto("", "Auto (Follow Game)"),
    English("en_us", "English"),
    ChineseSimplified("zh_cn", "简体中文"),
    Korean("ko_kr", "한국어"),
    Japanese("ja_jp", "日本語"),
    Russian("ru_ru", "Русский"),
    Custom("", "Custom");

    private final String code;
    private final String settingName;

    EpsilonLanguage(String code, String settingName) {
        this.code = code;
        this.settingName = settingName;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return settingName;
    }

    public boolean isCustom() {
        return this == Custom;
    }

    public boolean isAuto() {
        return this == Auto;
    }
}

package com.kuroneko.cqbot.enums;

public enum sysPluginRegex {
    TKF_SYSTEM("^(?i)(tkf|tarkov|塔科夫)(?<text>.*)"),
    BA_SYSTEM("^(?i)(ba|蔚蓝档案)(?<text>.*)"),


    OTHER("(?<text>.*)");

    public final String regex;

    sysPluginRegex(String regex) {
        this.regex = regex;
    }
}

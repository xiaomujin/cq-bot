package com.kuroneko.cqbot.utils;

import java.util.regex.Pattern;

public class RegexUtil {

    private static final String REGEX_NUM = "\\d+";

    public static void main(String[] args) {
        System.out.println(isNumber("1234s5678"));
    }

    public static boolean isNumber(String data) {
        return Pattern.matches(REGEX_NUM, data);
    }

}

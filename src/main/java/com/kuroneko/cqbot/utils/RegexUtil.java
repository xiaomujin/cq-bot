package com.kuroneko.cqbot.utils;

import com.kuroneko.cqbot.enums.Regex;
import com.mikuac.shiro.common.utils.RegexUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    private static final String REGEX_NUM = "\\d+";

    public static void main(String[] args) {
        Map<String, Pattern> cache = new ConcurrentHashMap<>();
        String input = "https://www.bilibili.com/video/BV17D4y1z7tW?";
        Pattern pattern = cache.computeIfAbsent(Regex.BILIBILI_BID, Pattern::compile);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String bid = matcher.group("bid");
            System.out.println("Bid: " + bid);
        } else {
            System.out.println("No match found.");
        }
    }

    public static boolean isNumber(String data) {
        return Pattern.matches(REGEX_NUM, data);
    }

    public static Optional<String> group(String group, String text, String regex) {
        Optional<Matcher> match = RegexUtils.matcher(regex, text);
        return match.map(v -> v.group(group));
    }

    public static Optional<String> group(int group, String text, String regex) {
        Optional<Matcher> match = RegexUtils.matcher(regex, text);
        return match.map(v -> v.group(group));
    }

    public static boolean matches(String text, String regex) {
        return text.matches(regex);
    }

}

package com.kuroneko.cqbot.utils;

public class NumberFormatter {

    /**
     * 格式化数字，根据数值大小自动使用“亿”、“万”或“个”作为单位
     *
     * @param number        要格式化的数字
     * @param decimalPlaces 保留的小数位数
     * @return 格式化后的字符串
     */
    public static String formatNumberWithUnit(double number, int decimalPlaces) {
        if (number >= 100_000_000) {
            double formatted = Math.round(number / 100_000_000 * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
            return formatted + "亿";
        } else if (number >= 10_000) {
            double formatted = Math.round(number / 10_000 * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
            return formatted + "万";
        } else if (number >= 1_000) {
            double formatted = Math.round(number / 1_000 * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
            return formatted + "千";
        } else {
            return String.valueOf((long) number); // 避免小数显示
        }
    }

    public static void main(String[] args) {
        System.out.println(formatNumberWithUnit(1234, 1));         // 输出: 1.2千
        System.out.println(formatNumberWithUnit(12345, 2));        // 输出: 1.23万
        System.out.println(formatNumberWithUnit(98765432, 1));     // 输出: 9876.5万
        System.out.println(formatNumberWithUnit(123456789, 2));    // 输出: 1.23亿
        System.out.println(formatNumberWithUnit(9999999999L, 1));  // 输出: 100.0亿
    }

}

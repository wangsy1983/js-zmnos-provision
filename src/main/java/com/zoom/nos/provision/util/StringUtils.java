package com.zoom.nos.provision.util;

/**
 * Created by mayss on 16/8/1.
 */
public class StringUtils {
    public static int getLimitLength(String s, int limit) {
        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        // 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
        for (int i = 0; i < s.length(); i++) {
            // 获取一个字符
            String temp = s.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
                // 中文字符长度为3
                valueLength += 3;
            } else {
                // 其他字符长度为1
                valueLength += 1;
            }
            if (valueLength == limit) {
                return i;
            }
            if (valueLength > limit) {
                return i - 1;
            }
        }
        //进位取整
        return  s.length();
    }
}

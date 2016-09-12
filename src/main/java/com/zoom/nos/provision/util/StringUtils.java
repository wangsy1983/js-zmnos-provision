package com.zoom.nos.provision.util;

/**
 * Created by mayss on 16/8/1.
 */
public class StringUtils {
    public static int getLimitLength(String s, int limit) {
        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        // ��ȡ�ֶ�ֵ�ĳ��ȣ�����������ַ�����ÿ�������ַ�����Ϊ2������Ϊ1
        for (int i = 0; i < s.length(); i++) {
            // ��ȡһ���ַ�
            String temp = s.substring(i, i + 1);
            // �ж��Ƿ�Ϊ�����ַ�
            if (temp.matches(chinese)) {
                // �����ַ�����Ϊ3
                valueLength += 3;
            } else {
                // �����ַ�����Ϊ1
                valueLength += 1;
            }
            if (valueLength == limit) {
                return i;
            }
            if (valueLength > limit) {
                return i - 1;
            }
        }
        //��λȡ��
        return  s.length();
    }
}

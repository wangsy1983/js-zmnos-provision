package com.zoom.nos.provision;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

/**
 * ����ת�����߳���
 * @author zm
 *
 */
public class CodeUtils {

	/**
	 * ��һ����ת��ʮ���Ƶ�Ascii��,�Կո�ָ�
	 * 
	 * @param number
	 * @return Ascii�루�ַ�����ʾ��
	 */
	public static String toAsciiDec(int number) {
		return CodeUtils.toAsciiDec(number, " ");
	}

	/**
	 * ��һ����ת��ʮ���Ƶ�Ascii��
	 * 
	 * @param number
	 * @param �ָ���
	 * @return Ascii�루�ַ�����ʾ��
	 */
	public static String toAsciiDec(int number, String separatorChar) {
		if (number >= 0 && number <= 9) {
			switch (number) {
			case (0):
				return "48";
			case (1):
				return "49";
			case (2):
				return "50";
			case (3):
				return "51";
			case (4):
				return "52";
			case (5):
				return "53";
			case (6):
				return "54";
			case (7):
				return "55";
			case (8):
				return "56";
			case (9):
				return "57";
			default:
				return "";
			}
		}

		StringBuffer asciiSb = new StringBuffer();

		StringBuffer _sb = new StringBuffer(Integer.toString(number));

		for (int i = 0; i < _sb.length(); i++) {
			if (i != 0) {
				asciiSb.append(separatorChar);
			}

			asciiSb.append(toAsciiDec(CharUtils.toIntValue(_sb.charAt(i)),
					separatorChar));
		}

		return asciiSb.toString();
	}

	/**
	 * test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(CodeUtils.toAsciiDec(12033," "));
		System.out.println(StringUtils.leftPad(CodeUtils.toAsciiDec(15, "."), 29, "48."));
		System.out.println(StringUtils.leftPad(CodeUtils.toAsciiDec(2159, "."), 29, "48."));
		
	}

}

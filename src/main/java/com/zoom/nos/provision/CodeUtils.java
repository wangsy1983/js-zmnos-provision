package com.zoom.nos.provision;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 编码转换工具程序
 * @author zm
 *
 */
public class CodeUtils {

	/**
	 * 把一个数转成十进制的Ascii码,以空格分隔
	 * 
	 * @param number
	 * @return Ascii码（字符串表示）
	 */
	public static String toAsciiDec(int number) {
		return CodeUtils.toAsciiDec(number, " ");
	}

	/**
	 * 把一个数转成十进制的Ascii码
	 * 
	 * @param number
	 * @param 分隔符
	 * @return Ascii码（字符串表示）
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

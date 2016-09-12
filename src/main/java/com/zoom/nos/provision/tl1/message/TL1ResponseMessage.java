package com.zoom.nos.provision.tl1.message;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.exception.TL1MessageParserException;

public class TL1ResponseMessage {
	private static Logger log = LoggerFactory
			.getLogger(TL1ResponseMessage.class);

	private TL1Header header = null;
	private TL1ResponseID responseID = null;
	private String textBlock = null;

	//
	protected String en;
	protected String enDesc;

	// completion code
	public final static String COMPLETED = "COMPLD";
	public final static String DENIED = "DENY";
	public final static String PARTIAL_SUCCESS = "PRTL";
	public final static String DELAYED_ACTIVATION = "DELAY";
	public final static String RETRIEVE = "RTRV";

	// <cr><lf><lf>^^^
	public final static String HeaderLine = new String(new char[] { 13, 10, 10,
			32, 32, 32 });
	// <cr><lf>M^^
	public final static String ResponseIdLine = new String(new char[] { 13, 10,
			'M', 32, 32 });
	// <cr><lf>*C
	public final static String AutoMsgTermCriticalAlarm = new String(
			new char[] { 13, 10, '*', 'C' });
	// <cr><lf>**
	public final static String AutoMsgTermMajorAlarm = new String(new char[] {
			13, 10, '*', '*' });
	// <cr><lf>*^
	public final static String AutoMsgTermMinorAlarm = new String(new char[] {
			13, 10, '*', 32 });
	// <cr><lf>A^
	public final static String AutoMsgTermNonAlarm = new String(new char[] {
			13, 10, 'A', 32 });
	// ^^^
	public final static String TextBlockHead = new String(new char[] { 32, 32, 32 });
	
	// <cr><lf>
	public final static String NewLine = new String(new char[] { 13, 10 });

	// Acknowledgement Messages
	// <cr><lf>IP^ In Progress
	// <cr><lf>OK^ All Right
	// <cr><lf>NA^ No Acknowledgement
	// <cr><lf>NG^ No Good
	// <cr><lf>RL^ Repeat Later

	/**
	 * <cr><lf>;
	 */
	public final static String Terminator = new String(
			new char[] { 13, 10, ';' });
	// <cr><lf>>
	public final static String TerminatorNotEnd = new String(new char[] { 13,
			10, '>' });

	/**
	 * 检查是否为通知消息
	 * 
	 * @param msg
	 * @return 如果是通知消息，返回true
	 */
	public static boolean isAutonomousMessage(String msg) {
		if (msg.indexOf(AutoMsgTermCriticalAlarm) != -1) {
			log.debug("CriticalAlarm");
			return true;
		} else if (msg.indexOf(AutoMsgTermMajorAlarm) != -1) {
			log.debug("MajorAlarm");
			return true;
		} else if (msg.indexOf(AutoMsgTermMinorAlarm) != -1) {
			log.debug("MinorAlarm");
			return true;
		} else if (msg.indexOf(AutoMsgTermNonAlarm) != -1) {
			log.debug("NonAlarm");
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param tl1ResponseText
	 * @return 解析后的消息
	 * @throws TL1MessageParserException
	 */
	public static TL1ResponseMessage parse(String tl1ResponseText)
			throws TL1MessageParserException {

		StringBuffer sb = new StringBuffer(tl1ResponseText);

		if (isAutonomousMessage(sb.toString())) {
			throw new TL1MessageParserException("this is Autonomous Messages");
		}

		TL1ResponseMessage resMsg = new TL1ResponseMessage();

		if (sb.indexOf(HeaderLine) != 0) {
			// 没找到 或 不是在开头的位置
			log.error("headerLine error in :" + sb);
			throw new TL1MessageParserException("parse headerLine error");
		}
		// remove headerLine
		splitSubString(sb, HeaderLine);

		// set header
		String headerText = splitSubString(sb, ResponseIdLine);
		TL1Header header = TL1Header.paser(headerText);
		// log.debug("headerText="+headerText);
		resMsg.setHeader(header);

		// set responseId
		String responseIdText = splitSubString(sb, NewLine);
		TL1ResponseID responseID = TL1ResponseID.paser(responseIdText);
		// log.debug("responseID="+responseID);
		resMsg.setResponseID(responseID);

		// set textBlock
		// 去掉3个空格
		splitSubString(sb, TextBlockHead);
		// log.debug("textBlock="+sb.toString());
		resMsg.setTextBlock(sb.toString());

		return resMsg;
	}

	/**
	 * 	分割字符串
	 * @param sb 
	 * @param str
	 * @return 指定字符前面的子串
	 */
	protected static String splitSubString(StringBuffer sb, String str) {
		int index = sb.indexOf(str);
		if (index == -1) {
			log.warn("Not found-[" + str + "] in [" + sb.toString() + "]");
			// not found
			return "";
		} else {
			String subString = sb.substring(0, index);
			sb.delete(0, index + str.length());
			return subString;
		}
	}

	/*
	 * TL1命令执行是否成功
	 */
	public boolean isSuccess() {
		if (COMPLETED.equalsIgnoreCase(getResponseID().getCompletionCode())) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * TL1命令执行是否失败
	 */
	public boolean isFailed() {
		return !isSuccess();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public TL1Header getHeader() {
		return header;
	}

	public void setHeader(TL1Header header) {
		this.header = header;
	}

	public TL1ResponseID getResponseID() {
		return responseID;
	}

	public void setResponseID(TL1ResponseID responseID) {
		this.responseID = responseID;
	}

	public String getTextBlock() {
		return textBlock;
	}

	public void setTextBlock(String textBlock) {
		this.textBlock = textBlock;
	}

	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en;
	}

	public String getEnDesc() {
		return enDesc;
	}

	public void setEnDesc(String enDesc) {
		this.enDesc = enDesc;
	}
}

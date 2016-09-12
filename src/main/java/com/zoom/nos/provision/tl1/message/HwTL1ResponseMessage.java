package com.zoom.nos.provision.tl1.message;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.exception.TL1MessageParserException;
import com.zoom.nos.provision.tl1.session.SystemFlag;

public class HwTL1ResponseMessage extends TL1ResponseMessage {

	private static Logger log = LoggerFactory.getLogger(HwTL1ResponseMessage.class);
	private Map<String, String> result = new HashMap<String, String>();

	public final String EN_PREFIX = "EN=";
	public final String ENDESC_PREFIX = "ENDESC=";
	public final String ERRORCODE_PREFIX = "ErrorCode:";
	
	private void putEditCmdReq13027(String text) throws TL1MessageParserException {
		int i = text.indexOf(ERRORCODE_PREFIX);
		if (i == -1) {
			putEditCmdReq(text);
			return;
		} else {
			if (text.lastIndexOf("(") > i && text.lastIndexOf(")") > i) {
//				EN=IRC   ENDESC=resource conflict(ONUNO) ErrorCode: 2688880284 (接口配置的参数冲突)
				en = text.substring(i + 10, text.lastIndexOf("("));
			} else {
//				EN=IRC   ENDESC=resource conflict(ONUNO) ErrorCode: 2688880284
				en = text.substring(i + 10, text.length()-1);
			}
			
			if (en != null && en.compareTo("") > 0) {
				en = EN_PREFIX + en.trim();
			}
			if (!StringUtils.startsWithIgnoreCase(en, EN_PREFIX)) {
				throw new TL1MessageParserException("not fonud " + EN_PREFIX);
			} else {
				en = en.substring(EN_PREFIX.length()).trim();
			}
			
			if (text.indexOf(ERRORCODE_PREFIX) > 0) {
				try{
					if(text.indexOf("(")>0 && text.indexOf(")") > i){
						enDesc = text.substring(text.lastIndexOf("(") + 1, text.lastIndexOf(")")).trim();
					}else{
						enDesc =text.substring(text.lastIndexOf(":") + 1, text.length()-1).trim();
					}
				}catch(Exception e){
					throw new TL1MessageParserException("not fonud error descr:" + ERRORCODE_PREFIX);
				}
			}
		}
		
	}

	private void putEditCmdReq(String text) throws TL1MessageParserException {
		// ^^^
		int i = text.indexOf("   ");
		if (i == -1) {
			throw new TL1MessageParserException("not fonud ^^^ in between");
		} else {
			en = text.substring(0, i);
			if (!StringUtils.startsWithIgnoreCase(en, EN_PREFIX)) {
				throw new TL1MessageParserException("not fonud " + EN_PREFIX);
			} else {
				en = en.substring(EN_PREFIX.length());
			}

			enDesc = text.substring(i + 3, text.length());
			if (!StringUtils.startsWithIgnoreCase(enDesc, ENDESC_PREFIX)) {
				throw new TL1MessageParserException("not fonud " + ENDESC_PREFIX);
			} else {
				enDesc = StringUtils.substringBetween(enDesc, ENDESC_PREFIX, "\r\n");
			}
		}
	}

	private void putListCmdReq(String text) throws TL1MessageParserException {
		// ^^^
		int ii = text.indexOf("   ");
		if (ii == -1) {
			throw new TL1MessageParserException("not fonud ^^^ in between");
		} else {
			en = text.substring(0, ii);
			if (!StringUtils.startsWithIgnoreCase(en, EN_PREFIX)) {
				throw new TL1MessageParserException("not fonud " + EN_PREFIX);
			} else {
				en = en.substring(EN_PREFIX.length());
			}

			enDesc = text.substring(ii + 3, text.length());
			if (!StringUtils.startsWithIgnoreCase(enDesc, ENDESC_PREFIX)) {
				throw new TL1MessageParserException("not fonud " + ENDESC_PREFIX);
			} else {
				enDesc = StringUtils.substringBetween(enDesc, ENDESC_PREFIX, "\r\n");
			}
			// get list info
			String[] lines = text.split("\r\n");
			for (int i = 0; i < lines.length - 2; i++) {
				String row = lines[i];
				// is table context ?
				if (row.indexOf("-----") != -1) {
					String[] attribs = lines[i + 1].split("\t");
					String[] values = lines[i + 2].split("\t");
					if (attribs.length != values.length) {
						log.error("list attribs.length != values.length");
						en = ErrorConst.TL1MessageParserErr+"";
						enDesc = "list attribs.length != values.length";
						return;
					}
					for (int j = 0; j < values.length; j++) {
						result.put(attribs[j], values[j]);
					}
					break;
				}
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, String> getResult() {
		return result;
	}

	/**
	 * 
	 * @param resMsg
	 * @return
	 * @throws TL1MessageParserException
	 */
	public static HwTL1ResponseMessage parse(TL1ResponseMessage resMsg) 
		throws TL1MessageParserException {
		HwTL1ResponseMessage hwTL1ResponseMessage=new HwTL1ResponseMessage();
		hwTL1ResponseMessage.setHeader(resMsg.getHeader());
		hwTL1ResponseMessage.setResponseID(resMsg.getResponseID());
		hwTL1ResponseMessage.setTextBlock(resMsg.getTextBlock());
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
			hwTL1ResponseMessage.putEditCmdReq13027(resMsg.getTextBlock());
		} else {
			hwTL1ResponseMessage.putEditCmdReq(resMsg.getTextBlock());
		}
		return hwTL1ResponseMessage;
	}
	
	/**
	 * 解析查询命令返回结果
	 * @param resMsg
	 * @return
	 * @throws TL1MessageParserException
	 */
	public static HwTL1ResponseMessage parseListCmd(TL1ResponseMessage resMsg)
		throws TL1MessageParserException {
		HwTL1ResponseMessage hwTL1ResponseMessage=new HwTL1ResponseMessage();
		
		hwTL1ResponseMessage.setHeader(resMsg.getHeader());
		hwTL1ResponseMessage.setResponseID(resMsg.getResponseID());
		hwTL1ResponseMessage.setTextBlock(resMsg.getTextBlock());
		hwTL1ResponseMessage.putListCmdReq(resMsg.getTextBlock());
		
		return hwTL1ResponseMessage;
	}

	/*
	 * Hw命令执行是否成功
	 * EN=0
	 */
	public boolean isSuccess(){
		if (super.isSuccess()) {
			if("0".equals(getEn())){
				return true;
			}else{
				return false;				
			}
		}else{
			return false;
		}
	}
	
	/*
	 * Hw命令执行是否失败
	 * EN!=0
	 */
	public boolean isFailed(){
		return !isSuccess();
	}
}

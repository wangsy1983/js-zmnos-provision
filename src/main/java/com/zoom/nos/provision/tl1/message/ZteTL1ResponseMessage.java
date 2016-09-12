package com.zoom.nos.provision.tl1.message;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.exception.TL1MessageParserException;

public class ZteTL1ResponseMessage  extends TL1ResponseMessage {
	private static Logger log = LoggerFactory.getLogger(ZteTL1ResponseMessage.class);

	private Map<String, String> result = new HashMap<String, String>();

	public final String EN_PREFIX = "EN=";
	public final String ENDESC_PREFIX = "ENDESC=";

	private void putEditCmdReq(String text) throws TL1MessageParserException {
		//EN=0^^^ENDESC=No Error
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
					String[] attribs = lines[i + 1].trim().split("  ");
					String[] values = lines[i + 2].trim().split("  ");
					if (attribs.length != values.length) {
						log.error("list attribs.length != values.length [" + attribs.length + "!="
								+ values.length + "]");
						en = ErrorConst.TL1MessageParserErr + "";
						enDesc = "List failed, length error";
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
	public static ZteTL1ResponseMessage parse(TL1ResponseMessage resMsg) 
	throws TL1MessageParserException {
		ZteTL1ResponseMessage zteTL1ResponseMessage = new ZteTL1ResponseMessage();

		zteTL1ResponseMessage.setHeader(resMsg.getHeader());
		zteTL1ResponseMessage.setResponseID(resMsg.getResponseID());
		zteTL1ResponseMessage.setTextBlock(resMsg.getTextBlock());
		zteTL1ResponseMessage.putEditCmdReq(resMsg.getTextBlock());

		return zteTL1ResponseMessage;
	}
	
	/**
	 * 解析查询命令返回结果
	 * @param resMsg
	 * @return
	 * @throws TL1MessageParserException
	 */
	public static ZteTL1ResponseMessage parseListCmd(TL1ResponseMessage resMsg)
		throws TL1MessageParserException {
		ZteTL1ResponseMessage zteTL1ResponseMessage=new ZteTL1ResponseMessage();
		
		zteTL1ResponseMessage.setHeader(resMsg.getHeader());
		zteTL1ResponseMessage.setResponseID(resMsg.getResponseID());
		zteTL1ResponseMessage.setTextBlock(resMsg.getTextBlock());
		zteTL1ResponseMessage.putListCmdReq(resMsg.getTextBlock());
		
		return zteTL1ResponseMessage;
	}
	

	/*
	 * Zte命令执行是否成功
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
	 * Zte命令执行是否失败
	 * EN!=0
	 */
	public boolean isFailed(){
		return !isSuccess();
	}
}

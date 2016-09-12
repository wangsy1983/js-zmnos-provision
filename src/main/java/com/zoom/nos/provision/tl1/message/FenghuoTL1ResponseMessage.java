package com.zoom.nos.provision.tl1.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.exception.TL1MessageParserException;

public class FenghuoTL1ResponseMessage extends TL1ResponseMessage {
	
	private static Logger log = LoggerFactory.getLogger(FenghuoTL1ResponseMessage.class);
	private Map<String, String> result = new HashMap<String, String>();
	private List<HashMap<String, String>> muResult = new ArrayList<HashMap<String, String>>();

	public final String EN_PREFIX = "EN=";
	public final String ENDESC_PREFIX = "ENDESC=";

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
			if (StringUtils.startsWithIgnoreCase(en, EN_PREFIX)) {
				en = en.substring(EN_PREFIX.length());
			}else{
				en = "0";
			}

			enDesc = text.substring(ii + 3, text.length());
			if (StringUtils.startsWithIgnoreCase(enDesc, ENDESC_PREFIX)) {
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


	private void putMuListCmdReq(String text) throws TL1MessageParserException {
		// ^^^
		int ii = text.indexOf("   ");
		if (ii == -1) {
			throw new TL1MessageParserException("not fonud ^^^ in between");
		} else {
			en = text.substring(0, ii);
			if (StringUtils.startsWithIgnoreCase(en, EN_PREFIX)) {
				en = en.substring(EN_PREFIX.length());
			}else{
				en = "0";
			}

			enDesc = text.substring(ii + 3, text.length());
			if (StringUtils.startsWithIgnoreCase(enDesc, ENDESC_PREFIX)) {
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
					HashMap<String, String> _rs = new HashMap<String, String>();
					for (int j = 0; j < values.length; j++) {
						_rs.put(attribs[j], values[j]);
					}
					muResult.add(_rs);
					
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
	 * @return
	 */
	public List<HashMap<String, String>> getMuResult() {
		return muResult;
	}
	/**
	 * 
	 * @param resMsg
	 * @return
	 * @throws TL1MessageParserException
	 */
	public static FenghuoTL1ResponseMessage parse(TL1ResponseMessage resMsg) 
		throws TL1MessageParserException {
		FenghuoTL1ResponseMessage fhTL1ResponseMessage=new FenghuoTL1ResponseMessage();
		
		fhTL1ResponseMessage.setHeader(resMsg.getHeader());
		fhTL1ResponseMessage.setResponseID(resMsg.getResponseID());
		fhTL1ResponseMessage.setTextBlock(resMsg.getTextBlock());
		fhTL1ResponseMessage.putEditCmdReq(resMsg.getTextBlock());
		
		return fhTL1ResponseMessage;
	}
	
	/**
	 * 解析查询命令返回结果
	 * @param resMsg
	 * @return
	 * @throws TL1MessageParserException
	 */
	public static FenghuoTL1ResponseMessage parseListCmd(TL1ResponseMessage resMsg)
		throws TL1MessageParserException {
		FenghuoTL1ResponseMessage fhTL1ResponseMessage=new FenghuoTL1ResponseMessage();
		
		fhTL1ResponseMessage.setHeader(resMsg.getHeader());
		fhTL1ResponseMessage.setResponseID(resMsg.getResponseID());
		fhTL1ResponseMessage.setTextBlock(resMsg.getTextBlock());
		fhTL1ResponseMessage.putListCmdReq(resMsg.getTextBlock());
		
		return fhTL1ResponseMessage;
	}
	
	/**
	 * 解析查询命令返回结果
	 * @param resMsg
	 * @return
	 * @throws TL1MessageParserException
	 */
	public static FenghuoTL1ResponseMessage parseMuListCmd(TL1ResponseMessage resMsg)
		throws TL1MessageParserException {
		FenghuoTL1ResponseMessage fhTL1ResponseMessage=new FenghuoTL1ResponseMessage();
		
		fhTL1ResponseMessage.setHeader(resMsg.getHeader());
		fhTL1ResponseMessage.setResponseID(resMsg.getResponseID());
		fhTL1ResponseMessage.setTextBlock(resMsg.getTextBlock());
		fhTL1ResponseMessage.putMuListCmdReq(resMsg.getTextBlock());
		
		return fhTL1ResponseMessage;
	}

	
	/*
	 * 命令执行是否成功
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
	 * 命令执行是否失败
	 * EN!=0
	 */
	public boolean isFailed(){
		return !isSuccess();
	}
}
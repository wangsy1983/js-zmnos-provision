package com.zoom.nos.provision.tl1.message;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.exception.TL1MessageParserException;

public class AlcatelTL1ResponseMessage extends TL1ResponseMessage {

	private static Logger log = LoggerFactory
			.getLogger(AlcatelTL1ResponseMessage.class);

	// free format text
	// /*
	private final String startFFtext = "/*";

	// */
	private final String endFFtext = "*/";
	
	//
	private Map<String, String> result = new HashMap<String, String>();

	private void putEditCmdReq(String text) throws TL1MessageParserException {
		log.debug("req=" + text);
		StringBuffer sb = new StringBuffer(text);
		
		splitSubString(sb, startFFtext);
		String commandEcho = splitSubString(sb, endFFtext);
		log.debug("commandEcho="+commandEcho);
		splitSubString(sb, TextBlockHead);
		//
		en = splitSubString(sb, NewLine);
		en = en.trim();
		log.debug("en="+en);
		splitSubString(sb, startFFtext);
		//
		enDesc = splitSubString(sb, endFFtext);
		log.debug("enDesc="+enDesc);
	}


	private void putListCmdReq(String text) throws TL1MessageParserException {
		// ^^^
		int ii = text.indexOf("   ");
		if (ii == -1) {
			throw new TL1MessageParserException("not fonud ^^^ in between");
		} else {
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
			if (result.isEmpty()) {
				en = ErrorConst.getOntNameFailed+"";
				enDesc = "get Ont Name Failed";
			}else{
				en = "0";
				enDesc = "success";
			}
			
		}
	}
	/**
	 * 
	 * @param resMsg
	 * @return
	 * @throws TL1MessageParserException
	 */
	public static AlcatelTL1ResponseMessage parse(TL1ResponseMessage resMsg)
			throws TL1MessageParserException {
		AlcatelTL1ResponseMessage alcatelTL1ResponseMessage = new AlcatelTL1ResponseMessage();

		alcatelTL1ResponseMessage.setHeader(resMsg.getHeader());
		alcatelTL1ResponseMessage.setResponseID(resMsg.getResponseID());
		alcatelTL1ResponseMessage.setTextBlock(resMsg.getTextBlock());
		if (alcatelTL1ResponseMessage.isFailed()) {
			alcatelTL1ResponseMessage.putEditCmdReq(resMsg.getTextBlock());
		}

		return alcatelTL1ResponseMessage;
	}
	
	
	/**
	 * 解析查询命令返回结果
	 * @param resMsg
	 * @return
	 * @throws TL1MessageParserException
	 */
	public static AlcatelTL1ResponseMessage parseListCmd(TL1ResponseMessage resMsg)
		throws TL1MessageParserException {
		AlcatelTL1ResponseMessage tl1ResponseMessage=new AlcatelTL1ResponseMessage();
		
		tl1ResponseMessage.setHeader(resMsg.getHeader());
		tl1ResponseMessage.setResponseID(resMsg.getResponseID());
		tl1ResponseMessage.setTextBlock(resMsg.getTextBlock());
		tl1ResponseMessage.putListCmdReq(resMsg.getTextBlock());
		
		return tl1ResponseMessage;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Map<String, String> getResult() {
		return result;
	}

	
	public static void main(String [] args){
		AlcatelTL1ResponseMessage a=new AlcatelTL1ResponseMessage(); 
		try {
			a.putEditCmdReq("   /* ED-HSI:ONUIP=10.1.44.50:HSIPORT-1-1:33::BWRATEUP=3072,BWRATEDN=3072 */\r\n   IOTO\r\n   /* Input operation time out */\r\n;");
		} catch (TL1MessageParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

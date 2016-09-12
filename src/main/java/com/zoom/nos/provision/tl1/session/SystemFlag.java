package com.zoom.nos.provision.tl1.session;

//import java.util.ResourceBundle;

import com.zoom.nos.provision.core.CoreService;

public class SystemFlag {
	private SystemFlag() {
	}

	public static String getSystemFlag() {
		String localstate = CoreService.ticketControlService.getLocalState();
		if (localstate != null && (localstate.equals("江苏") || localstate.equals("JS"))) {
			return "js.unicom";
		} else if (localstate != null && (localstate.equals("青海") || localstate.equals("QH"))) {
			return "qh.unicom";
		} else if (localstate != null && (localstate.equals("辽宁") || localstate.equals("LN"))) {
			return "ln.unicom";
		}
		return "";
	}

	/**联通标识*/
	public static String JS_UNICOM ="js.unicom";
	public static String LN_UNICOM ="ln.unicom";
	public static String QH_UNICOM ="qh.unicom";
}

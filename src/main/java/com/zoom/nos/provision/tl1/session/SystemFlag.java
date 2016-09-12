package com.zoom.nos.provision.tl1.session;

//import java.util.ResourceBundle;

import com.zoom.nos.provision.core.CoreService;

public class SystemFlag {
	private SystemFlag() {
	}

	public static String getSystemFlag() {
		String localstate = CoreService.ticketControlService.getLocalState();
		if (localstate != null && (localstate.equals("����") || localstate.equals("JS"))) {
			return "js.unicom";
		} else if (localstate != null && (localstate.equals("�ຣ") || localstate.equals("QH"))) {
			return "qh.unicom";
		} else if (localstate != null && (localstate.equals("����") || localstate.equals("LN"))) {
			return "ln.unicom";
		}
		return "";
	}

	/**��ͨ��ʶ*/
	public static String JS_UNICOM ="js.unicom";
	public static String LN_UNICOM ="ln.unicom";
	public static String QH_UNICOM ="qh.unicom";
}

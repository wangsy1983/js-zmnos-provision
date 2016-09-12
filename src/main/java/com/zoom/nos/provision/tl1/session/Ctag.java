package com.zoom.nos.provision.tl1.session;

/**
 * 
 * @author zm
 * 提供静态getCtag方法
 * </P>
 * 1~999990 six characters
 */
public class Ctag {


	// 1~999990 six characters
	private static int ctag = 0;
	
	//
	private Ctag(){		
	}
	
	/**
	 * get ctag,1~999990 six characters
	 * 
	 * @return
	 */
	public synchronized static int getCtag() {
		if (ctag >= 999990) {
			return 1;
		} else {
			ctag++;
			return ctag;
		}
	}
}

package com.zoom.nos.provision.tl1.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.TL1ResponseMessage;
import com.zoom.nos.provision.tl1.message.ZteTL1ResponseMessage;

public class ZteTl1Session extends Tl1Session {
	private static Logger log = LoggerFactory.getLogger(ZteTl1Session.class);

	/**
	 * create zte tl1 session
	 * 
	 * @param serverIp
	 * @param serverPort
	 * @param localIp
	 * @param localPort
	 * @param user
	 * @param password
	 * @param timeout
	 */
	public ZteTl1Session(String serverIp, int serverPort, String localIp,
			int localPort, String user, String password, int timeout) {
		super(serverIp, serverPort, localIp, localPort, user, password, timeout);
	}

	/**
	 * 
	 */
	public ZteTL1ResponseMessage exeZteCmd(String cmd, WorkOrder wo)
			throws ZtlException {
		TL1ResponseMessage resMsg = exeCmd(cmd);

		ZteTL1ResponseMessage msg = ZteTL1ResponseMessage.parse(resMsg);
		// log cmd
		CoreService.ticketControlService.insertCmdlog(cmd, wo, msg);

		return msg;
	}

	/**
	 * 
	 */
	public ZteTL1ResponseMessage exeZteListCmd(String cmd, WorkOrder wo)
			throws ZtlException {
		TL1ResponseMessage resMsg = exeCmd(cmd);

		ZteTL1ResponseMessage msg = ZteTL1ResponseMessage.parseListCmd(resMsg);
		// log cmd
		CoreService.ticketControlService.insertCmdlog(cmd, wo, msg);

		return msg;
	}

	/**
	 * 
	 */
	@Override
	public void login() throws ZtlException {
		StringBuffer cmd = new StringBuffer();
		// ACT-USER::root:1000::admin;
		cmd.append("ACT-USER::").append(getUser()).append(":").append(
				Ctag.getCtag()).append("::").append(getPassword()).append(";");
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
			//江苏联通中兴网管暂时不需要登陆
			TL1ResponseMessage remsg = exeCmd(cmd.toString());
			if (remsg.isFailed()) {
//				throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
			}
		} else {
			TL1ResponseMessage remsg = exeCmd(cmd.toString());
			if (remsg.isFailed()) {
				throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
			}
		}
	}

	@Override
	public void logout() throws ZtlException {
		// CANC-USER::$username:CTAG::;
		StringBuffer cmd = new StringBuffer();
		cmd.append("CANC-USER::").append(getUser()).append(":").append(
				Ctag.getCtag()).append("::;");
		TL1ResponseMessage remsg = exeCmd(cmd.toString());
		if (remsg.isFailed()) {
			throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
		}
	}
}

package com.zoom.nos.provision.tl1.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.HwTL1ResponseMessage;
import com.zoom.nos.provision.tl1.message.TL1ResponseMessage;


public class HuaweiTl1Session extends Tl1Session {
	private static Logger log = LoggerFactory.getLogger(HuaweiTl1Session.class);

	/**
	 * 
	 * @param serverIp
	 * @param serverPort
	 * @param localIp
	 * @param localPort
	 * @param user
	 * @param password
	 * @param timeout
	 */
	public HuaweiTl1Session(String serverIp, int serverPort, String localIp,
			int localPort, String user, String password, int timeout) {
		super(serverIp, serverPort, localIp, localPort, user, password, timeout);
	}

	/**
	 * 
	 * @param cmd
	 * @param wo - loggin  WorkOrderId, OriginWoId
	 * @return
	 * @throws ZtlException
	 */
	public HwTL1ResponseMessage exeHwCmd(String cmd, WorkOrder wo) throws ZtlException {
		TL1ResponseMessage resMsg = super.exeCmd(cmd);

		HwTL1ResponseMessage msg = HwTL1ResponseMessage.parse(resMsg);
		//log cmd
		CoreService.ticketControlService.insertCmdlog(cmd, wo, msg);
		
		return msg;
	}

	/**
	 * 
	 * @param cmd
	 * @param wo - loggin  WorkOrderId, OriginWoId
	 * @return
	 * @throws ZtlException
	 */
	public HwTL1ResponseMessage exeHwListCmd(String cmd, WorkOrder wo)
			throws ZtlException {
		TL1ResponseMessage resMsg = super.exeCmd(cmd);

		HwTL1ResponseMessage msg = HwTL1ResponseMessage.parseListCmd(resMsg);
		//log cmd
		CoreService.ticketControlService.insertCmdlog(cmd, wo, msg);

		return msg;
	}
	/**
	 * huawei open session
	 * 
	 * @throws ZtlException
	 */
	@Override
	public void login() throws ZtlException {
		// LOGIN:::Ctag::UN=huawei,PWD=N2000user;
		StringBuffer cmd = new StringBuffer();
		cmd.append("LOGIN:::").append(Ctag.getCtag()).append("::UN=").append(getUser()).append(",PWD=")
				.append(getPassword()).append(";");
		TL1ResponseMessage remsg = super.exeCmd(cmd.toString());
		if (remsg.isFailed()) {
			throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
		}
	}

	/**
	 * huawei close session
	 * 
	 * 
	 */
	@Override
	public void logout() throws ZtlException {
		// LOGOUT:::Ctag::;
		StringBuffer cmd = new StringBuffer();
		cmd.append("LOGOUT:::").append(Ctag.getCtag()).append("::;");
		TL1ResponseMessage remsg = super.exeCmd(cmd.toString());
		if (remsg.isFailed()) {
			throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
		}
	}
	
	/**
	 * 
	 * @param cmd
	 * @param wo - loggin  WorkOrderId, OriginWoId
	 * @return
	 * @throws ZtlException
	 */
	public String exeHwCmdForPonType(String devIP,String frameNum,String slot) throws ZtlException {
		TL1ResponseMessage resMsg = super.exeCmd("LST-BOARD::DEV="+devIP+",FN="+frameNum+",SN="+slot+":"+Ctag.getCtag()+"::;"); 
		return resMsg.getTextBlock(); 
	}

}
package com.zoom.nos.provision.tl1.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.FenghuoTL1ResponseMessage;
import com.zoom.nos.provision.tl1.message.TL1ResponseMessage;

public class FenghuoTl1Session extends Tl1Session {
	private static Logger log = LoggerFactory.getLogger(FenghuoTl1Session.class);

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
	public FenghuoTl1Session(String serverIp, int serverPort, String localIp,
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
	public FenghuoTL1ResponseMessage exeFenghuoCmd(String cmd, WorkOrder wo) throws ZtlException {
		TL1ResponseMessage resMsg = exeCmd(cmd);

		FenghuoTL1ResponseMessage msg = FenghuoTL1ResponseMessage.parse(resMsg);
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
	public FenghuoTL1ResponseMessage exeFhListCmd(String cmd, WorkOrder wo)
			throws ZtlException {
		TL1ResponseMessage resMsg = super.exeCmd(cmd);

		FenghuoTL1ResponseMessage msg = FenghuoTL1ResponseMessage.parseListCmd(resMsg);
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
	public FenghuoTL1ResponseMessage exeFhMuListCmd(String cmd, WorkOrder wo)
			throws ZtlException {
		TL1ResponseMessage resMsg = super.exeCmd(cmd);

		FenghuoTL1ResponseMessage msg = FenghuoTL1ResponseMessage.parseMuListCmd(resMsg);
		//log cmd
		CoreService.ticketControlService.insertCmdlog(cmd, wo, msg);

		return msg;
	}
	
	/**
	 * 
	 */
	@Override
	public void login() throws ZtlException {
		StringBuffer cmd = new StringBuffer();
		// LOGIN:::CTAG::UN=fh_tl1,PWD=ANM2000tl1; 
		cmd.append("LOGIN:::");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("UN=").append(getUser());
		cmd.append(",PWD=").append(getPassword());
		cmd.append(";");
		TL1ResponseMessage remsg = exeCmd(cmd.toString());
		if (remsg.isFailed()) {
			throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
		}
	}

	@Override
	public void logout() throws ZtlException {
		StringBuffer cmd = new StringBuffer();
		// LOGOUT:::CTAG::; 
		cmd.append("LOGOUT:::");
		cmd.append(Ctag.getCtag());
		cmd.append("::;");
		TL1ResponseMessage remsg = exeCmd(cmd.toString());
		if (remsg.isFailed()) {
			throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
		}
	}
}
package com.zoom.nos.provision.tl1.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.AlcatelTL1ResponseMessage;
import com.zoom.nos.provision.tl1.message.TL1ResponseMessage;

public class AlcatelTl1Session extends Tl1Session {
	private static Logger log = LoggerFactory.getLogger(AlcatelTl1Session.class);

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
	public AlcatelTl1Session(String serverIp, int serverPort, String localIp,
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
	public AlcatelTL1ResponseMessage exeAlcatelCmd(String cmd, WorkOrder wo) throws ZtlException {
		TL1ResponseMessage resMsg = exeCmd(cmd);

		AlcatelTL1ResponseMessage msg = AlcatelTL1ResponseMessage.parse(resMsg);
		//log cmd
		if(wo != null){
			CoreService.ticketControlService.insertCmdlog(cmd, wo, msg);
		}
		return msg;
	}

	/**
	 * 
	 * @param cmd
	 * @param wo - loggin  WorkOrderId, OriginWoId
	 * @return
	 * @throws ZtlException
	 */
	public AlcatelTL1ResponseMessage exeAlcatelListCmd(String cmd, WorkOrder wo)
			throws ZtlException {
		TL1ResponseMessage resMsg = super.exeCmd(cmd);

		AlcatelTL1ResponseMessage msg = AlcatelTL1ResponseMessage.parseListCmd(resMsg);
		//log cmd
		CoreService.ticketControlService.insertCmdlog(cmd, wo, msg);

		return msg;
	}
	
	/**
	 * 
	 * @throws ZtlException
	 */
	@Override
	public void login() throws ZtlException {
		// LOGIN-USER:::1::USERNAME=manager,PASSWORD=123456;
		
		//JS LOGIN:::CTAG::UN=admin,PWD=ans#150;
		StringBuffer cmd = new StringBuffer();
		cmd.append("LOGIN:::CTAG::UN=")
		.append(getUser()).append(",PWD=").append(getPassword()).append(";");
		
//		cmd.append("LOGIN-USER:::").append(Ctag.getCtag()).append("::USERNAME=")
//				.append(getUser()).append(",PASSWORD=").append(getPassword()).append(";");
		TL1ResponseMessage remsg = super.exeCmd(cmd.toString());
		if (remsg.isFailed()) {
			throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
		}
	}

	/**
	 * close session
	 * 
	 */
	@Override
	public void logout() throws ZtlException {
		// LOGOUT-USER:::1::USERNAME=manager,PASSWORD=123456;
		StringBuffer cmd = new StringBuffer();
		
		cmd.append("LOGOUT-USER:::").append(Ctag.getCtag()).append("::USERNAME=")
			.append(getUser()).append(",PASSWORD=").append(getPassword()).append(";");
		
		TL1ResponseMessage remsg = super.exeCmd(cmd.toString());
		if (remsg.isFailed()) {
			throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
		}
	}


}
package com.zoom.nos.provision.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.session.HuaweiTl1Session;

public class gongdanutil {
	private static Logger log = LoggerFactory.getLogger(gongdanutil.class);
	/**
	 * ��ȡolt��ĳ���˿�����
	 * @param null
	 * @return str
	 */
	public String getPonType(WorkOrder wo) throws ZtlException {
		if (wo == null) {
			return "";
		}
		
		String ponType = "";
		//ĿǰJS��ֻͨ�ܻ�Ϊ������PON����
		if (wo.getDeviceMaker() != null || "��Ϊ".equals(wo.getDeviceMaker())
				|| "HUAWEI".equalsIgnoreCase(wo.getDeviceMaker())) {
			// ��Ϊ������ȡ�˿�����Ϊ�ֱ����GPON����EPON
			ponType = this.executeQuery(wo.getTl1ServerIp(),wo.getTl1ServerPort(), wo.getTl1User(),wo.getTl1Password(),
					wo.getNeIp(),String.valueOf(wo.getFrameId()),String.valueOf(wo.getSlotId()));
		}
		log.debug(wo.getNeIp()+" ȡ�� olt �˿����� "+ponType);
	    return ponType;
	}
	
	/**
	 * ����AD���ٶ�Ӧ
	 * 1m-6m ����20%
	 */
	public static int getQuickenRate(int oldRate) {
		switch (oldRate) {
		case (1024):
			return 1216;
		case (2048):
			return 2464;
		case (3072):
			return 3680;
		case (4096):
			return 4928;
		case (5120):
			return 6144;
		case (6144):
			return 7360;
		default:
			return oldRate;
		}
	}

	/**
	 * ���豸������ȡ��GPON or EPON
	 * 
	 * @param tl1ServerIp
	 * @param tl1ServerPort
	 * @param tl1User
	 * @param tl1Password
	 * @return pontype
	 */
	private String executeQuery(String tl1ServerIp,
			int tl1ServerPort, String tl1User, String tl1Password,String deviceIP,String FrameNum,String Slot) {
 
		HuaweiTl1Session session = null;

		session = new HuaweiTl1Session(tl1ServerIp, tl1ServerPort, "", 0,
				tl1User, tl1Password, NosEnv.socket_timeout_tl1server * 15);
		// open session
		try {
			session.open();

			String dids = session.exeHwCmdForPonType(deviceIP,FrameNum,Slot);
			 log.debug(dids);

			String[] lines = dids.split("\n");
			log.debug("row count:" + lines.length);
			if (lines.length < 1) {
				log.error("lines.length < 1");
				return "";// ��map
			}

			for (int i = 0; i < 10 && i < lines.length; i++) {
				// ������Ϣ����ӡǰ10��
				log.debug(lines[i]);
			}
			// ������Ϣ����ӡ���һ��
			log.debug(lines[lines.length - 1]);

			if (! (";".equals(lines[lines.length - 1])) ) {
				log.error("[not find Terminator ;]" + lines[lines.length - 1]);
				return "";// ��map
			}
			
			boolean isContext = false;
			for (int i = 0; i < lines.length; i++) {
				String row = lines[i];
				try { 
					// is table context ?
					if (row.indexOf("-----") > -1) {
						if (isContext == false) {
							// star table context
							isContext = true;
							// skip tow row head
							i = i + 2;
						} else {
							// table context end
							isContext = false;
						}
					}
//					log.info(" isContext="+isContext);
					// is table ? if (isContext || lines[i].lastIndexOf(".")-lines[i].indexOf(".")>0) {
					if (lines[i]!=null && lines[i].lastIndexOf(".")-lines[i].indexOf(".")>0) { 
						row = lines[i]; 
						String[] vals = row.split("\t");
						return vals[3].trim(); 
					}
				} catch (Exception e) {
					log.error("parse pon type error:" + row);
					log.error(e.toString(), e);
				}
			}

		} catch (Exception e) {
			log.error(e.toString(), e);
		} finally {
			// �ͷ���Դ
			if (session != null) {
				try {
					session.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return "";
	}
}

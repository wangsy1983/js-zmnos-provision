package com.zoom.nos.provision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NosEnv {
	private static Logger log = LoggerFactory.getLogger(NosEnv.class);
	
	
	// ͨ�ý�������ʱʱ��
	public static long device_generalswitch_timeout = 5000;
	// ͨ�ý�������ʱ���Դ���
	public static int device_generalswitch_retries = 4;
	
	// �����豸��ʱʱ��
	public static long device_huanyudslam_timeout = 5000;
	// �����豸��ʱ���Դ���
	public static int device_huanyudslam_retries = 4;
	
	//snmp���������
	public static int snmp_max_connection = 10;

	/** 
	 * ����1k�ĵ�λ
	 */
	public static int ratek_unit = 1000;

	// Ĭ�ϵ�����������ʣ�kbps��
	public static int default_AturRate = 640;


	//------------------------------------------
	// ����DB��ȡ����ִ�й����ļ������λ�Ǻ��롣
	public static int takeMainDBWorkOrder_period = 600*1000;

	// �ӱ���DB��ȡ����ִ�й����ļ������λ�Ǻ��롣
	public static int takeLocalDBWorkOrder_period = 10*1000;
	
	// ��ʱ����ʧ�ܹ����ļ������λ�Ǻ��롣
	public static int takeFaildDBWorkOrder_period = 30*60*1000;
	
	//------------------------------------------
	
	
	// ���ݿ�������key�Ļ����С����������ʱ���˷�һЩ�����е�key
	public static int incrementerCacheSize = 10;
	
	// ��tl1server֮��ĳ�ʱʱ��(����)��Ĭ��60��
	public static int socket_timeout_tl1server = 60000;
	

	// ��ֹ��ʵ����
	private NosEnv() {

	}
	
}

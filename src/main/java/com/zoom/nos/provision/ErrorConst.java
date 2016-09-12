package com.zoom.nos.provision;

public class ErrorConst {

	private ErrorConst() {
	}

	/** ִ�гɹ��� */
	public static final long success = 0;

	/** SNMP���� */
	public static final long SNMP_ERROR = 98;

	/** SNMP����ʱ */
	public static final long SNMP_TIMEOUT = 99;

	/** ��Ϊ�豸��ǰ�˿�״̬�쳣���������ڴ��Ķ˿�״̬�� */
	public static final long portStatusAbnormal = 301;

	/** ��Ϊ�豸��������������� */
	public static final long commandSyntaxErr = 302;

	/** �����豸�����ڡ� */
	public static final long IDNE = 303;

	/** �����豸��Ų����ڡ� */
	public static final long IBNE = 304;

	/** �����豸�˿ںŲ����ڡ� */
	public static final long IPNE = 305;

	/** �����豸��·�����ڡ� */
	public static final long IVCNE = 306;

	/** �����豸��·ģ�岻���ڡ� */
	public static final long IPFNE = 307;

	/** �����豸ȱ�ٲ����� */
	public static final long IMP = 308;

	/** �����豸�������� */
	public static final long IIPE = 309;

	/** �����豸���ܲ�֧�ִ˲����� */
	public static final long DDNS = 310;

	/** �����豸����ʧ�ܡ� */
	public static final long DDOF = 311;

	/** Alcatel�豸����ִ��ʧ�ܡ� */
	public static final long AlcatelCmdErr = 314;

	/** TL1��������½��ǳ�ʧ�ܡ� */
	public static final long Tl1ServerLoginErr = 300;

	/** TL1������TCP���Ӵ��� */
	public static final long TCPErr = 313;

	/** TL1������TCPͨ�Ŵ��� */
	public static final long TCPIOErr = 315;

	/** DSLAM�豸�����ڡ� */
	public static final long noSuchDslam = 1000;

	/** ����״̬���Ϸ��� */
	public static final long wrongServiceStatus = 1001;

	/** ����������ʲ��Ϸ��� */
	public static final long wrongRate = 1002;

	/** ����ģ�����Զ���ͨϵͳ��δ��ȷ���á� */
	public static final long lineProfileNoutFound = 1003;

	/** �豸����ģ���Ҳ����������ʼ�����ݡ� */
	public static final long configProfileNoutFound = 1004;

	/** �豸�����ϲ����ڸ��豸�ı�ʶ�ţ������豸���ܵ����û����³�ʼ���豸��ʶ����Ϣ�� */
	public static final long noSuchDid = 1005;

	/** ��ͨ�豸����Ϣ��ȫ����TL1���������豸���ҡ�ͨ������ģʽ����Ϣδ�ҵ��� */
	public static final long incompleteInfo = 1006;

	/** �˿ڲ����� */
	public static final long portNotExist = 1007;

	/** TL1����ؽ�������쳣 */
	public static final long TL1MessageParserErr = 3001;
	
	/** FTTH��֧�� */
	public static final long wrongFtth = 2901;

	/** �ó����豸�ݲ�֧�� */
	public static final long wrongDeviceMaker = 2902;
	
	/** ���豸�ͺŲ�֧�� */
	public static final long wrongDeviceType = 2903;	
	
	/** �ó����豸�ݲ�֧�� */
	public static final long  lackResParam = 2904;

	/** TL1ִ�й����г����쳣 */
	public static final long UnknowError = 9999;
	

	/** OID���Ȳ��ܴ���ʮ */
	public static final long oidTooLength  = 2037;
	
	/** TIDֻ����Ϊ����  */
	public static final long tidNeedNumber  = 2038;
	
	
	/** SbcIp����Ϊ��  */
	public static final long sbcIpNotBlank  = 2039;
	
	/** SbcIpReserve����Ϊ��  */
	public static final long sbcIpReserveNotBlank  = 2040;
	
	/** ��·��ҵ��ģ��δ��ȷ���� */
	public static final long lineSrvProfNoutFound = 2603;
	
	/** cvlan����Ϊ��  */
	public static final long cvlanNotBlank  = 2016;
	
	/** svlan����Ϊ��  */
	public static final long svlanNotBlank  = 2015;

	/** vlan����Ϊ��  */
	public static final long vlanNotBlank  = 2017;

	/** voicevlan����Ϊ��  */
	public static final long voicevlanNotBlank  = 2018;
	
	/** ��������ȡONT NAMEʧ��  */
	public static final long getOntNameFailed  = 2604;
	
	/** ONT��ID�ظ������������ϲ�ͬ  */
	public static final long repeatOntIdOnDevAdmin  = 2605;
	
	/**iptv����,ONU��δע�� */
	public static final long iptvNotFindONU  = 2606;
}

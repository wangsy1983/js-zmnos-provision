package com.zoom.nos.provision;

public class ErrorConst {

	private ErrorConst() {
	}

	/** 执行成功。 */
	public static final long success = 0;

	/** SNMP错误 */
	public static final long SNMP_ERROR = 98;

	/** SNMP请求超时 */
	public static final long SNMP_TIMEOUT = 99;

	/** 华为设备当前端口状态异常，不是所期待的端口状态。 */
	public static final long portStatusAbnormal = 301;

	/** 华为设备命令错误或参数错误。 */
	public static final long commandSyntaxErr = 302;

	/** 中兴设备不存在。 */
	public static final long IDNE = 303;

	/** 中兴设备板号不存在。 */
	public static final long IBNE = 304;

	/** 中兴设备端口号不存在。 */
	public static final long IPNE = 305;

	/** 中兴设备电路不存在。 */
	public static final long IVCNE = 306;

	/** 中兴设备线路模板不存在。 */
	public static final long IPFNE = 307;

	/** 中兴设备缺少参数。 */
	public static final long IMP = 308;

	/** 中兴设备参数错误。 */
	public static final long IIPE = 309;

	/** 中兴设备可能不支持此操作。 */
	public static final long DDNS = 310;

	/** 中兴设备操作失败。 */
	public static final long DDOF = 311;

	/** Alcatel设备命令执行失败。 */
	public static final long AlcatelCmdErr = 314;

	/** TL1服务器登陆或登出失败。 */
	public static final long Tl1ServerLoginErr = 300;

	/** TL1服务器TCP连接错误。 */
	public static final long TCPErr = 313;

	/** TL1服务器TCP通信错误。 */
	public static final long TCPIOErr = 315;

	/** DSLAM设备不存在。 */
	public static final long noSuchDslam = 1000;

	/** 服务状态不合法。 */
	public static final long wrongServiceStatus = 1001;

	/** 下行最大速率不合法。 */
	public static final long wrongRate = 1002;

	/** 速率模板在自动开通系统中未正确配置。 */
	public static final long lineProfileNoutFound = 1003;

	/** 设备配置模板找不到。请检查初始化数据。 */
	public static final long configProfileNoutFound = 1004;

	/** 设备网管上不存在该设备的标识号，请检查设备网管的配置或重新初始化设备标识号信息。 */
	public static final long noSuchDid = 1005;

	/** 开通设备的信息不全，如TL1服务器、设备厂家、通道工作模式等信息未找到。 */
	public static final long incompleteInfo = 1006;

	/** 端口不存在 */
	public static final long portNotExist = 1007;

	/** TL1命令返回结果解析异常 */
	public static final long TL1MessageParserErr = 3001;
	
	/** FTTH不支持 */
	public static final long wrongFtth = 2901;

	/** 该厂家设备暂不支持 */
	public static final long wrongDeviceMaker = 2902;
	
	/** 该设备型号不支持 */
	public static final long wrongDeviceType = 2903;	
	
	/** 该厂家设备暂不支持 */
	public static final long  lackResParam = 2904;

	/** TL1执行过程中出现异常 */
	public static final long UnknowError = 9999;
	

	/** OID长度不能大于十 */
	public static final long oidTooLength  = 2037;
	
	/** TID只允许为数字  */
	public static final long tidNeedNumber  = 2038;
	
	
	/** SbcIp不能为空  */
	public static final long sbcIpNotBlank  = 2039;
	
	/** SbcIpReserve不能为空  */
	public static final long sbcIpReserveNotBlank  = 2040;
	
	/** 线路、业务模板未正确配置 */
	public static final long lineSrvProfNoutFound = 2603;
	
	/** cvlan不能为空  */
	public static final long cvlanNotBlank  = 2016;
	
	/** svlan不能为空  */
	public static final long svlanNotBlank  = 2015;

	/** vlan不能为空  */
	public static final long vlanNotBlank  = 2017;

	/** voicevlan不能为空  */
	public static final long voicevlanNotBlank  = 2018;
	
	/** 从网管上取ONT NAME失败  */
	public static final long getOntNameFailed  = 2604;
	
	/** ONT的ID重复，名字网管上不同  */
	public static final long repeatOntIdOnDevAdmin  = 2605;
	
	/**iptv工单,ONU尚未注册 */
	public static final long iptvNotFindONU  = 2606;
}

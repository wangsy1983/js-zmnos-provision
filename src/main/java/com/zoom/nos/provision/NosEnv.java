package com.zoom.nos.provision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NosEnv {
	private static Logger log = LoggerFactory.getLogger(NosEnv.class);
	
	
	// 通用交换机超时时间
	public static long device_generalswitch_timeout = 5000;
	// 通用交换机超时重试次数
	public static int device_generalswitch_retries = 4;
	
	// 环宇设备超时时间
	public static long device_huanyudslam_timeout = 5000;
	// 环宇设备超时重试次数
	public static int device_huanyudslam_retries = 4;
	
	//snmp最大连接数
	public static int snmp_max_connection = 10;

	/** 
	 * 速率1k的单位
	 */
	public static int ratek_unit = 1000;

	// 默认的最大上行速率（kbps）
	public static int default_AturRate = 640;


	//------------------------------------------
	// 从主DB中取可以执行工单的间隔，单位是毫秒。
	public static int takeMainDBWorkOrder_period = 600*1000;

	// 从本地DB中取可以执行工单的间隔，单位是毫秒。
	public static int takeLocalDBWorkOrder_period = 10*1000;
	
	// 定时重做失败工单的间隔，单位是毫秒。
	public static int takeFaildDBWorkOrder_period = 30*60*1000;
	
	//------------------------------------------
	
	
	// 数据库自增长key的缓存大小，程序重启时会浪费一些缓存中的key
	public static int incrementerCacheSize = 10;
	
	// 与tl1server之间的超时时间(毫秒)：默认60秒
	public static int socket_timeout_tl1server = 60000;
	

	// 禁止类实例化
	private NosEnv() {

	}
	
}

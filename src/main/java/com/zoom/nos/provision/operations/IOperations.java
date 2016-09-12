package com.zoom.nos.provision.operations;

import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.exception.ZtlException;

public interface IOperations {

	/**
	 * 开宽带上网
	 * 
	 * @return 执行结果
	 */
	public WoResult open() throws ZtlException;

	/**
	 * 关宽带上网
	 * 
	 * @return 执行结果
	 */
	public WoResult close()throws ZtlException;

	/**
	 * 修改速率
	 * 
	 * @return 执行结果
	 */
	public WoResult alterRate() throws ZtlException;

	/**
	 * 释放资源
	 */
	public void destruction();
	

	/**
	 * 开通IPTV业务
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult openIptv() throws ZtlException;

	/**
	 * 关闭IPTV业务
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult closeIptv() throws ZtlException;

	/**
	 * 开通宽带和IPTV业务
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult openWBIptv() throws ZtlException;
	
	/**
	 * 关闭宽带和IPTV业务
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult closeWBIptv() throws ZtlException;
	
	/**
	 * 开通Voip语言业务
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult openVoip() throws ZtlException;

	/**
	 * 关闭Voip语言业务
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult closeVoip() throws ZtlException;

	/**
	 * 开通视频监控业务
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult openVideo() throws ZtlException;

	/**
	 * 关闭视频监控业务
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult closeVideo() throws ZtlException;
	
	/**
	 * 注册ONU
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult registerOnu() throws ZtlException;	
	
	/**
	 * 注销ONU
	 * 
	 * @return 执行结果
	 * @throws ZtlException
	 */
	public WoResult delOnu() throws ZtlException;	
}

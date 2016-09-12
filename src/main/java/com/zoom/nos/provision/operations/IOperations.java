package com.zoom.nos.provision.operations;

import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.exception.ZtlException;

public interface IOperations {

	/**
	 * ���������
	 * 
	 * @return ִ�н��
	 */
	public WoResult open() throws ZtlException;

	/**
	 * �ؿ������
	 * 
	 * @return ִ�н��
	 */
	public WoResult close()throws ZtlException;

	/**
	 * �޸�����
	 * 
	 * @return ִ�н��
	 */
	public WoResult alterRate() throws ZtlException;

	/**
	 * �ͷ���Դ
	 */
	public void destruction();
	

	/**
	 * ��ͨIPTVҵ��
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult openIptv() throws ZtlException;

	/**
	 * �ر�IPTVҵ��
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult closeIptv() throws ZtlException;

	/**
	 * ��ͨ�����IPTVҵ��
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult openWBIptv() throws ZtlException;
	
	/**
	 * �رտ����IPTVҵ��
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult closeWBIptv() throws ZtlException;
	
	/**
	 * ��ͨVoip����ҵ��
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult openVoip() throws ZtlException;

	/**
	 * �ر�Voip����ҵ��
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult closeVoip() throws ZtlException;

	/**
	 * ��ͨ��Ƶ���ҵ��
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult openVideo() throws ZtlException;

	/**
	 * �ر���Ƶ���ҵ��
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult closeVideo() throws ZtlException;
	
	/**
	 * ע��ONU
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult registerOnu() throws ZtlException;	
	
	/**
	 * ע��ONU
	 * 
	 * @return ִ�н��
	 * @throws ZtlException
	 */
	public WoResult delOnu() throws ZtlException;	
}

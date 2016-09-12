package com.zoom.nos.provision.snmp;


import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpHandler {
	private static Logger log = LoggerFactory.getLogger(SnmpHandler.class);

	// snmp read community
	private String readCommunity = "public";

	// snmp write community
	private String writeCommunity = "private";

	// port
	private int port = 161;

	// IP address
	private InetAddress ipAddress;

	//
	private CommunityTarget target;

	private TransportMapping transport;

	private Snmp snmp;

	// snmp version
	private int snmpVersion = SnmpConstants.version1;

	// snmp time out
	private long timeout = 5000;

	// retry times
	private int retries = 4;

	/**
	 * create a snmpHandler, use UDP protocol
	 * 
	 * Ĭ��snmpVersion =1 ; Timeout = 5s ; retries = 4 ; port =161
	 * 
	 * @param inetAddress
	 */
	public SnmpHandler(InetAddress inetAddress) {
		this.ipAddress = inetAddress;
	}

	/**
	 * create a snmpHandler, use UDP protocol
	 * 
	 * Ĭ��snmpVersion =1 ; Timeout = 5s ; retries = 4
	 * 
	 * @param inetAddress
	 * @param port
	 */
	public SnmpHandler(InetAddress inetAddress, int port) {
		this.ipAddress = inetAddress;
		this.port = port;
	}

	/**
	 * 
	 * @param inetAddress
	 * @param port
	 * @param timeout
	 * @param retries
	 */
	public SnmpHandler(InetAddress inetAddress, int port, long timeout,
			int retries) {
		this.ipAddress = inetAddress;
		this.port = port;
		this.timeout = timeout;
		this.retries = retries;
	}

	/**
	 * open
	 * 
	 * @throws SnmpHandlerException
	 * 
	 */
	public void open() throws SnmpHandlerException {
		// target
		target = new CommunityTarget();
		target.setAddress(new UdpAddress(ipAddress, port));
		target.setTimeout(timeout);
		target.setRetries(retries);
		target.setVersion(snmpVersion);
		try {
			// udp transport
			transport = new DefaultUdpTransportMapping();
			// listen response
			transport.listen();
		} catch (IOException e) {
			throw new SnmpHandlerException(e.toString());
		}
		//
		snmp = new Snmp(transport);
	}

	/**
	 * snmp get ��һֵ
	 */
	public Variable get(String oid) throws SnmpHandlerException {
		// call get(OID oid):Variable
		return this.get(new OID(oid));
	}

	/**
	 * snmp set ��һֵ
	 */
	public Variable set(String oid, Variable variable)
			throws SnmpHandlerException {
		// call get(OID oid):Variable
		return this.set(new OID(oid), variable);
	}

	/**
	 * snmp get ����һֵ
	 * 
	 * @param oid
	 * @return ����oidȡ����ֵ
	 * @throws SnmpHandlerException
	 */
	public Variable get(OID oid) throws SnmpHandlerException {
		log.debug(ipAddress.getHostAddress() + " " + readCommunity
				+ ", get OID : " + oid.toString());

		PDU reqestPdu = new PDU();

		reqestPdu.add(new VariableBinding(oid));

		// set Community
		target.setCommunity(new OctetString(readCommunity));

		// snmpget
		ResponseEvent responseEvn = null;
		try {
			responseEvn = snmp.get(reqestPdu, target);
		} catch (IOException e) {
			throw new SnmpHandlerException(e.toString());
		}

		if (responseEvn != null && responseEvn.getResponse() != null) {
			PDU responsePdu = responseEvn.getResponse();
			if (responsePdu.getErrorStatus() == PDU.noError) {
				if (responsePdu.size() > 0) {
					VariableBinding varBinding = responsePdu.get(0);
					Variable v = varBinding.getVariable();
					return v;
				}
			} else {
				// respone error
				throw new SnmpHandlerException(responsePdu);
			}
		} else {
			// time out request
			throw SnmpHandlerException.generateTimeOutSnmpHandlerException();
		}
		// not value
		return null;
	}

	/**
	 * snmp set ����һֵ
	 * 
	 * @param oid
	 * @param variable
	 * @return set����ֵ
	 * @throws SnmpHandlerException
	 */
	public Variable set(OID oid, Variable variable) throws SnmpHandlerException {
		log.info(ipAddress.getHostAddress() + " " + writeCommunity
						+ ", set OID : " + oid.toString() + " = "
						+ variable.toString());

		PDU reqestPdu = new PDU();

		reqestPdu.add(new VariableBinding(oid, variable));
		//
		target.setCommunity(new OctetString(writeCommunity));

		// snmpset
		ResponseEvent responseEvn = snmp.set(reqestPdu, target);

		if (responseEvn != null && responseEvn.getResponse() != null) {
			PDU responsePdu = responseEvn.getResponse();
			if (responsePdu.getErrorStatus() == PDU.noError) {
				if (responsePdu.size() > 0) {
					VariableBinding varBinding = responsePdu.get(0);
					Variable v = varBinding.getVariable();
					return v;
				}
			} else {
				// respone error
				throw new SnmpHandlerException(responsePdu);
			}
		} else {
			// time out request
			throw SnmpHandlerException.generateTimeOutSnmpHandlerException();
		}
		// not value
		return null;
	}

	/**
	 * snmp set ��ֵ
	 * 
	 * @param OID
	 *            , Variable map
	 * @return set����ֵ
	 * @throws SnmpHandlerException
	 */
	public VariableBinding[] set(Map<OID, Variable> ovmap)
			throws SnmpHandlerException {
		PDU reqestPdu = new PDU();

		for (Map.Entry<OID, Variable> ov : ovmap.entrySet()) {
			log.debug(ipAddress.getHostAddress() + " " + writeCommunity
					+ ", set OID : " + ov.getKey().toString() + " = "
					+ ov.getValue().toString());
			reqestPdu.add(new VariableBinding(ov.getKey(), ov.getValue()));
		}

		//
		target.setCommunity(new OctetString(writeCommunity));

		// snmpset
		ResponseEvent responseEvn = snmp.set(reqestPdu, target);

		if (responseEvn != null && responseEvn.getResponse() != null) {
			PDU responsePdu = responseEvn.getResponse();
			if (responsePdu.getErrorStatus() == PDU.noError) {
				return responsePdu.toArray();
			} else {
				// respone error
				throw new SnmpHandlerException(responsePdu);
			}
		} else {
			// time out request
			throw SnmpHandlerException.generateTimeOutSnmpHandlerException();
		}
		// not value
		// return null;
	}

	/**
	 * snmp get ��ֵ
	 * 
	 * @param oid
	 *            list
	 * @return ����oidȡ����ֵ
	 * @throws SnmpHandlerException
	 */
	public VariableBinding[] get(List<OID> oids) throws SnmpHandlerException {
		PDU reqestPdu = new PDU();

		for (OID oid : oids) {
			reqestPdu.add(new VariableBinding(oid));
		}

		// set Community
		target.setCommunity(new OctetString(readCommunity));

		// snmpget
		ResponseEvent responseEvn = null;
		try {
			responseEvn = snmp.get(reqestPdu, target);
		} catch (IOException e) {
			throw new SnmpHandlerException(e.toString());
		}

		if (responseEvn != null && responseEvn.getResponse() != null) {
			PDU responsePdu = responseEvn.getResponse();
			if (responsePdu.getErrorStatus() == PDU.noError) {
				return responsePdu.toArray();
			} else {
				// respone error
				throw new SnmpHandlerException(responsePdu);
			}
		} else {
			// time out request
			throw SnmpHandlerException.generateTimeOutSnmpHandlerException();
		}
		// not value
		// return null;
	}

	/**
	 * test method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		InetAddress address;
		SnmpHandler handler = null;
		try {
			address = InetAddress.getByName("192.168.1.15");
			handler = new SnmpHandler(address);
			handler.setTimeout(3000);
			handler.setRetries(3);

			handler.setReadCommunity("public");
			handler.setWriteCommunity("cde3");

			handler.open();
			Variable v1 = handler.set("1.3.6.1.2.1.1.5.0", new OctetString(
					"zoo44oom"));

			System.out.println(v1.toString());

			Variable v2 = handler.get("1.3.6.1.2.1.1.5.0");

			System.out.println(v2.toString());

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SnmpHandlerException e) {
			e.printStackTrace();
			System.out.println(e.getErrorStatus());
		} finally {
			if (handler != null) {
				handler.close();
			}
		}
	}

	/**
	 * close snmp
	 * 
	 */
	public void close() {
		try {
			if (snmp != null) {
				snmp.close();
			}
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}

	// //////////////////////////////////

	public String getReadCommunity() {
		return readCommunity;
	}

	/**
	 * ���� snmp ��������
	 * 
	 * @param readCommunity
	 */
	public void setReadCommunity(String readCommunity) {
		this.readCommunity = readCommunity;
	}

	public String getWriteCommunity() {
		return writeCommunity;
	}

	/**
	 * ���� snmp д������
	 * 
	 * @param writeCommunity
	 */
	public void setWriteCommunity(String writeCommunity) {
		this.writeCommunity = writeCommunity;
	}

	/**
	 * �������ӵ����豸snmp IP��ַ
	 * <p>
	 * ������open����ǰ����
	 * 
	 * @param ipAddress
	 */
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}

	/**
	 * �������ӵ����豸snmp�˿ں�
	 * <p>
	 * ������open����ǰ����
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public long getTimeout() {
		return timeout;
	}

	/**
	 * ���ó�ʱʱ��
	 * <p>
	 * ������open����ǰ����
	 * 
	 * @param snmpTimeout
	 */
	public void setTimeout(long snmpTimeout) {
		this.timeout = snmpTimeout;
	}

	public int getSnmpVersion() {
		return snmpVersion;
	}

	/**
	 * ����snmp�汾
	 * <p>
	 * SnmpConstants.version1
	 * <p>
	 * SnmpConstants.version2c
	 * <p>
	 * SnmpConstants.version3
	 * <p>
	 * ������open����ǰ����
	 * 
	 * @param snmpVersion
	 */
	public void setSnmpVersion(int snmpVersion) {
		this.snmpVersion = snmpVersion;
	}

	public int getRetries() {
		return retries;
	}

	/**
	 * ����snmp���Դ���
	 * <p>
	 * ������open����ǰ����
	 * 
	 * @param retries
	 */
	public void setRetries(int retries) {
		this.retries = retries;
	}
}

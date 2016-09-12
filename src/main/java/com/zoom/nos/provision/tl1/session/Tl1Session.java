package com.zoom.nos.provision.tl1.session;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.TL1ResponseMessage;

public abstract class Tl1Session {

	private static Logger log = LoggerFactory.getLogger(Tl1Session.class);

	// tl1 server ip
	private String serverIp = "";

	// tl1 server port
	private int serverPort = 0;

	// ��ʱʱ��(����)��Ĭ��120��
	private int timeout = 120000;

	// local IP address
	private String localIp = "";
	// local port
	private int localPort = 20001;

	// user
	private String user = "";
	// password
	private String password = "";

	// client socket
	private Socket socket = null;

	// <cr><lf>;
	public final String endString = TL1ResponseMessage.Terminator;

	/**
	 * 
	 * @param serverIp
	 * @param serverPort
	 * @param localIp
	 * @param localPort
	 * @param user
	 * @param password
	 * @param timeout
	 *            ����Ϊ��λ
	 */
	public Tl1Session(String serverIp, int serverPort, String localIp,
			int localPort, String user, String password, int timeout) {
		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.localIp = localIp;
		this.localPort = localPort;
		this.user = user;
		this.password = password;
		this.timeout = timeout;
	}

	/**
	 * open session
	 * 
	 * @throws ZtlExeException
	 */
	public void open() throws ZtlException {
		if (socket != null) {
			try {
				this.close();
				socket = null;
			} catch (Exception e) {
			}
		}
		try {
			// ���� tl1Server socket
			socket = new Socket();
			if (StringUtils.isNotBlank(localIp)) {
				// �󶨱��ص�ַ
				socket.bind(new InetSocketAddress(localIp, localPort));
			}
			// set ��ʱʱ��
			socket.setSoTimeout(timeout);
			// ����
			socket.connect(new InetSocketAddress(serverIp, serverPort));
			log.debug("connect to "+serverIp+":"+serverPort+" t="+timeout);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ZtlException(ErrorConst.TCPErr);
		}
		try {
			// ��¼����
			login();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
		}
	}

	/**
	 * close session
	 * 
	 */
	public void close() {
		// logout
		try {
			logout();
		} catch (Exception e) {
			log.error(serverIp + " session logout failed", e);
		}
		// close socket
		if (socket != null) {
			try {
				socket.close();
				log.debug(serverIp + " session socket closed");
			} catch (Exception e) {
				log.error(serverIp + " session socket close failed", e);
			}
			socket = null;
		}
	}

	/**
	 * login
	 * 
	 * @throws ZtlException
	 */
	public abstract void login() throws Exception;

	/**
	 * logou
	 * 
	 * @throws ZtlException
	 */
	public abstract void logout() throws Exception;

	/**
	 * ִ��TL1�������TL1ResponseMessage
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException
	 */
	public synchronized TL1ResponseMessage exeCmd(String cmd)
			throws ZtlException {
		try {
			log.debug("cmd=" + cmd);
			String remsgText = "";
			// ��������
			Writer write = new OutputStreamWriter(socket.getOutputStream());
			write.write(cmd);
			write.flush();
			// ������Ϣ
			remsgText = this.read(endString);

			while (TL1ResponseMessage.isAutonomousMessage(remsgText)) {
				// �����֪ͨ��Ϣ��ȡ��һ��������Ϣ
				// ��ȡ��һ��������Ϣ��
				remsgText = this.read(endString);
				// ֪ͨ��Ϣ�Ĵ���Ŀǰû��
			}
			// ����֪ͨ��Ϣ,����ΪTL1ResponseMessage
			TL1ResponseMessage msg = TL1ResponseMessage.parse(remsgText);
			return msg;
		}catch (SocketTimeoutException e) {
			log.error(e.toString(), e);
			throw new ZtlException(ErrorConst.TCPIOErr);
		} catch (ZtlException e) {
			log.error(e.toString(), e);
			throw e;
		} catch (IOException e) {
			log.error(e.toString(), e);
			throw new ZtlException(ErrorConst.TCPIOErr);
		} catch (Exception e) {
			log.error(e.toString(), e);
			throw new ZtlException(ErrorConst.UnknowError);
		}
	}

	/**
	 * ���豸�������
	 * 
	 * @param endFlag
	 *            ��ֹ��־�ַ�
	 * @return �Ѿ�������ַ���
	 * @throws IOException ����IO�������ϼ�������׽
	 */
	private String read(String endFlag) {
		try{
			int length = 0;
			int DATA_SEG_LEN = 2 * 1024;
			byte byteBuffer[] = new byte[DATA_SEG_LEN];
			StringWriter swr = new StringWriter();
			String tmps = null;
	
			while ((length = socket.getInputStream().read(byteBuffer, 0,
					DATA_SEG_LEN)) != -1) {
				//debug
	//			debugBytes(byteBuffer);
				//
				tmps = new String(byteBuffer, 0, length);
				// log.debug(tmps);
				swr.write(tmps.toCharArray(), 0, tmps.toCharArray().length);
				if (tmps.indexOf(endFlag) != -1) {
					break;
				}
			}

			log.debug("reMsg=" + swr.toString());
			return swr.toString();
		} catch (IOException e) {
			log.error(e.toString(), e);
			try {
				throw new ZtlException(ErrorConst.TCPIOErr);
			} catch (ZtlException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return e.toString();
		}
	}
	
	/**
	 * ��ӡbyte����
	 * @param byteBuffer
	 */
	private void debugBytes(byte [] byteBuffer){
		StringBuffer _byte=new StringBuffer();
		for (byte b : byteBuffer) {
			if(b != 0){
				_byte.append("["+Integer.toHexString(b)+"]");
			}
		}
		log.debug("Bytes="+_byte);
	}

	/**
	 * session is open?
	 * 
	 * @return
	 */
	public boolean isOpen() {
		// OReilly Java Network Programming 3rd Edition �ṩ�ķ���
		boolean connected = socket.isConnected() && !socket.isClosed();
		return connected;
	}

	/**
	 * get ServerIp
	 * 
	 * @return
	 */
	public String getServerIp() {
		return serverIp;
	}

	/**
	 * get ServerPort
	 * 
	 * @return
	 */
	public int getServerPort() {
		return serverPort;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

}
package com.zoom.nos.provision;

import java.security.Provider;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class EncryptData {

	private byte[] encryptKey;
	private DESedeKeySpec spec;
	private SecretKeyFactory keyFactory;
	private SecretKey theKey;
	private Cipher cipher;
	private IvParameterSpec ivParameters;
	
	public EncryptData() {
		
		try {
			// ����Ƿ��� TripleDES ���ܵĹ�Ӧ����
			// ���ޣ���ȷ�ذ�װSunJCE ��Ӧ����
			try {
				Cipher c = Cipher.getInstance("DESede");
			} catch (Exception e) {
				System.err.println("Installling SunJCE provider.");
				Provider sunjce = new com.sun.crypto.provider.SunJCE();
				Security.addProvider(sunjce);
			}
			// ����һ����Կ
			encryptKey = "when i see you smile it feels like i'm falling".getBytes();

			// Ϊ��һ��Կ����һ��ָ���� DESSede key
			spec = new DESedeKeySpec(encryptKey);

			// �õ� DESSede keys
			keyFactory = SecretKeyFactory.getInstance("DESede");

			// ����һ�� DESede ��Կ����
			theKey = keyFactory.generateSecret(spec);

			// ����һ�� DESede ����
			cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");

			// Ϊ CBC ģʽ����һ�����ڳ�ʼ���� vector ����
			ivParameters = new IvParameterSpec(new byte[] { 12, 34, 56, 78, 90, 87, 65, 43 });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����
	 */
	public String encrypt(String password) {
		try {
			// �Լ���ģʽ��ʼ����Կ
			cipher.init(Cipher.ENCRYPT_MODE, theKey, ivParameters);

			byte[] bytes = cipher.doFinal(password.getBytes());
			return Base64.encode(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * �����㷨
	 */
	public String decrypt(String str) {

		try {
			// �Խ���ģʽ��ʼ����Կ
			cipher.init(Cipher.DECRYPT_MODE, theKey, ivParameters);

			byte[] decrypted_pwd = cipher.doFinal(Base64.decode(str));
			return new String(decrypted_pwd);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
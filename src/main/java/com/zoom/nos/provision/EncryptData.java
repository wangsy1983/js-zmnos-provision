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
			// 检测是否有 TripleDES 加密的供应程序
			// 如无，明确地安装SunJCE 供应程序
			try {
				Cipher c = Cipher.getInstance("DESede");
			} catch (Exception e) {
				System.err.println("Installling SunJCE provider.");
				Provider sunjce = new com.sun.crypto.provider.SunJCE();
				Security.addProvider(sunjce);
			}
			// 创建一个密钥
			encryptKey = "when i see you smile it feels like i'm falling".getBytes();

			// 为上一密钥创建一个指定的 DESSede key
			spec = new DESedeKeySpec(encryptKey);

			// 得到 DESSede keys
			keyFactory = SecretKeyFactory.getInstance("DESede");

			// 生成一个 DESede 密钥对象
			theKey = keyFactory.generateSecret(spec);

			// 创建一个 DESede 密码
			cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");

			// 为 CBC 模式创建一个用于初始化的 vector 对象
			ivParameters = new IvParameterSpec(new byte[] { 12, 34, 56, 78, 90, 87, 65, 43 });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加密
	 */
	public String encrypt(String password) {
		try {
			// 以加密模式初始化密钥
			cipher.init(Cipher.ENCRYPT_MODE, theKey, ivParameters);

			byte[] bytes = cipher.doFinal(password.getBytes());
			return Base64.encode(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解密算法
	 */
	public String decrypt(String str) {

		try {
			// 以解密模式初始化密钥
			cipher.init(Cipher.DECRYPT_MODE, theKey, ivParameters);

			byte[] decrypted_pwd = cipher.doFinal(Base64.decode(str));
			return new String(decrypted_pwd);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
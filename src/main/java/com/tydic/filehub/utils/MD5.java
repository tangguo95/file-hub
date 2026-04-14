package com.tydic.filehub.utils;

import java.security.MessageDigest;

/**
 * MD5加密
 */
public class MD5 {
	private final static String[] hexDigits = {
		"0", "1", "2", "3", "4", "5", "6", "7",
		"8", "9", "a", "b", "c", "d", "e", "f"
	};
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer sBuffer = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sBuffer.append(byteToHexString(b[i]));
		}
		return sBuffer.toString();
	}
	public static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n/16;
		int d2 = n%16;
		return hexDigits[d1] + hexDigits[d2];
	}
	
	public static String compile(String origin) {
		String resultString = null;
		try {
			resultString = new String(origin);
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
		} catch (Exception e) {
		}
		return resultString;
	}
	
	public static void main(String[] args) {
		String msg="APP_ID1006SERVICE_NAMEsiUserStateTIMESTAMP2017-06-02 18:04:44.121TRANS_ID201706021804441212984569f036aa0c0938237fd59c769fb48ec7b";
		System.out.println(MD5.compile(msg));
	}
}

/**
 * 
 */
package com.tydic.filehub.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zhangrong
 *
 *         2018年9月6日
 */
public class Wget {

	public static void main(String[] args) {
		Long beginTime = System.currentTimeMillis();
		String url = "http://localhost/example.jpg";
		String file = "./example.jpg";
		try {
			wget(url, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Time:["
				+ new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
				+ "],耗时:" + (System.currentTimeMillis() - beginTime));
	}

	/**
	 * 将HTTP资源另存为文件
	 * 
	 * @param destUrl
	 *            http地址 String
	 * @param fileName
	 *            文件路徑 String
	 * @throws Exception
	 */
	public static void wget(String destUrl, String fileName) throws IOException {
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		HttpURLConnection httpUrl = null;
		URL url = null;
		int BUFFER_SIZE = 4096;
		byte[] buf = new byte[BUFFER_SIZE];
		int size = 0;
		File f = new File(fileName);
		f.getParentFile().mkdirs();
		// 建立链接
		url = new URL(destUrl);
		httpUrl = (HttpURLConnection) url.openConnection();
		// 连接指定的资源
		httpUrl.connect();
		// 获取网络输入流
		bis = new BufferedInputStream(httpUrl.getInputStream());
		// 建立文件
		fos = new FileOutputStream(fileName);

		// 保存文件
		while ((size = bis.read(buf)) != -1)
			fos.write(buf, 0, size);

		fos.close();
		bis.close();
		httpUrl.disconnect();
	}
}

package com.tydic.filehub.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

public class Base64Image {
	public static String GetImageStr(String imgFilePath) {
		byte[] data = null;
		try (InputStream in = new FileInputStream(imgFilePath)) {
			data = new byte[in.available()];
			in.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Base64.getEncoder().encodeToString(data);
	}

	public static boolean GenerateImage(String imgStr, String imgFilePath) {
		if (imgStr == null) {
			return false;
		}
		try {
			byte[] bytes = Base64.getDecoder().decode(imgStr);
			File file = new File(imgFilePath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			try (OutputStream out = new FileOutputStream(imgFilePath)) {
				out.write(bytes);
				out.flush();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private Base64Image() {
	}
}

package com.lx.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileUtils {
	public static String getFileContent(String path) {
		StringBuffer st = new StringBuffer();
		FileReader rd = null;
		BufferedReader br = null;
		try {
			rd = new FileReader(path);
			br = new BufferedReader(rd);
			String line = null;
			while ((line = br.readLine()) != null) {
				st.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				rd.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return st.toString();
	}

	// 特殊文件 处理
	public static boolean isStatic(String uri) {
		boolean isStatic = false;
		String[] suffixs = { "html", "css", "js", "jpg", "jpeg", "png", "gif" };
		for (String suffix : suffixs) {
			if (uri.endsWith("." + suffix)) {
				isStatic = true;
				break;
			}
		}
		return isStatic;
	}
}

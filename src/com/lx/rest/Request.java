package com.lx.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * 
 * @author lixiang
 *
 */
public class Request {
	private String uri;
	private String pString;
	private HashMap<String, String> paramMap = new HashMap<String, String>();

	public Request(InputStream in) throws IOException {
		byte[] buff = new byte[1024];
		int len = in.read(buff);
		if (len > 0) {
			String msg = new String(buff, 0, len);
			createReuest(msg);
		}
	}

	public Request(String msg) throws IOException {
		createReuest(msg);
	}

	public String getParamName(String key) {
		return paramMap.get(key);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	private void createReuest(String msg) {
		System.out.println("请求报文开始---" + msg + "---请求报文结束");
		if (msg == null || msg.equals("")) {
			return;
		}
		// 截取uri路径
		int start = msg.indexOf("GET") + 4;
		if (msg.indexOf("POST") != -1) {
			start = msg.indexOf("POST") + 5;
		}
		int end = msg.indexOf("HTTP/1.1") - 1;
		uri = msg.substring(start, end);
		System.out.println("---------------uri=" + uri);
		if (msg.startsWith("POST")) {
			pString = msg.substring(msg.lastIndexOf("\n") + 1);
			String[] parms = pString.split("&");
			for (String parm : parms) {
				String[] keyValue = parm.split("=");
				paramMap.put(keyValue[0], keyValue[1].trim());
			}
		} else {
			pString = msg.substring(msg.lastIndexOf("?") + 1);
			String[] parms = pString.split("&");
			for (String parm : parms) {
				String[] keyValue = parm.split("=");
				String[] paramVale = keyValue[1].split(" ");
				paramMap.put(keyValue[0], paramVale[0]);
			}
		}
	}
}

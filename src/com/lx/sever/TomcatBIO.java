package com.lx.sever;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.lx.controller.LoginServlet;
import com.lx.rest.Request;
import com.lx.rest.Response;
import com.lx.util.FileUtils;

/**
 * 最原始的方式一个客户端分配一个线程（阻塞的io）
 * 
 * @author lixiang
 *
 */
public class TomcatBIO {
	public static void main(String[] args) {
		int count = 0;
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(8888);
			while (true) {
				Socket socket = ss.accept();
				count++;
				System.out.println("---你第" + count + "进入了服务器---");
				// =======拿到请求信息
				InputStream in = socket.getInputStream();
				Request request = new Request(in);
				// =======拿到响应信息
				OutputStream out = socket.getOutputStream();
				Response response = new Response(out);
				// =======业务逻辑
				String uri = request.getUri();
				System.out.println("=========" + uri);
				if (FileUtils.isStatic(uri)) {
					response.writerFile(uri.substring(1));
				} else if (uri.indexOf(".action") != -1) {
					LoginServlet loginServlet = new LoginServlet();
					loginServlet.service(request, response);
				}

				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

package com.lx.sever;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lx.controller.LoginServlet;
import com.lx.rest.Request;
import com.lx.rest.Response;
import com.lx.util.FileUtils;

/**
 * 这个版本采用线程池的处理客户链接（阻塞的io） Created by LiXiang on 2017/7/22.
 */
public class TomcatExecutorBIO {
	private static ExecutorService service = Executors.newCachedThreadPool();

	public static void main(String[] args) {
		int count = 0;
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(8888);
			while (true) {
				Socket socket = ss.accept();
				count++;
				System.out.println("---你第" + count + "进入了服务器---");
				service.submit(new HandleMsg(socket));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class HandleMsg implements Runnable {
		Socket clientSocket;

		public HandleMsg(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			InputStream in = null;
			OutputStream out = null;
			try {
				// =======拿到请求信息
				in = clientSocket.getInputStream();
				Request request = new Request(in);
				// =======拿到响应信息
				out = clientSocket.getOutputStream();
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
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

}

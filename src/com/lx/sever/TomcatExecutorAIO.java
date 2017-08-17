package com.lx.sever;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.lx.controller.LoginServlet;
import com.lx.rest.Request;
import com.lx.rest.Response;
import com.lx.util.FileUtils;

/**
 * Created by LiXiang on 2017/7/31. aio版的tomcat
 */
public class TomcatExecutorAIO {
	private final static int port = 8888;
	private AsynchronousServerSocketChannel server;

	public TomcatExecutorAIO() throws IOException {
		ExecutorService executorService = Executors.newCachedThreadPool();
		AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1);
		server = AsynchronousServerSocketChannel.open(threadGroup).bind(new InetSocketAddress(port));
	}

	public static void main(String[] args) throws Exception {
		new TomcatExecutorAIO().startServer();
		while (true) {
			Thread.sleep(1000);
		}

	}

	public void startServer() throws Exception {
		// 注册事件和事件完成后的处理器
		server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
			final ByteBuffer buffer = ByteBuffer.allocate(1024);

			@Override
			public void completed(AsynchronousSocketChannel result, Object attachment) {
				System.out.println(Thread.currentThread().getName());
				Future<Integer> writeResult = null;
				try {
					buffer.clear();
					result.read(buffer).get(1000, TimeUnit.SECONDS);
					buffer.flip();
					//
					Request request = new Request(new String(buffer.array()));
					Response response = new Response(result);
					String uri = request.getUri();
					if (uri == null || uri.equals("")) {
						return;
					}
					System.out.println("请求的路径" + uri);
					if (FileUtils.isStatic(uri)) {
						writeResult = response.writerFileByAIO(uri.substring(1));
					} else if (uri.indexOf(".action") != -1) {
						LoginServlet loginServlet = new LoginServlet();
						loginServlet.service(request, response);
					}
					// writeResult = result.write(buffer);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {

					try {
						server.accept(null, this);
						writeResult.get();
						result.close();
					} catch (Exception e) {

					}
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				System.out.println("failed" + exc);
			}
		});
	}

}

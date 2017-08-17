package com.lx.sever;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lx.controller.LoginServlet;
import com.lx.rest.Request;
import com.lx.rest.Response;
import com.lx.util.FileUtils;

/**
 * 这个版本采用的是多线程 nio的模式 Created by LiXiang on 2017/7/22.
 */
public class TomcatExecutorNIO {

	private Selector selector;
	private ExecutorService service = Executors.newCachedThreadPool();

	public static void main(String[] args) throws Exception {
		TomcatExecutorNIO tomcat2 = new TomcatExecutorNIO();
		tomcat2.startServer(8888);
	}

	private void startServer(int port) throws Exception {
		// 获得一个ServerSocket通道
		ServerSocketChannel ssc = ServerSocketChannel.open();
		// 设置成非阻塞
		ssc.configureBlocking(false);
		// 将该通道对应的ServerSocket绑定到port端口
		InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
		ssc.socket().bind(inetSocketAddress);
		// 获得一个通道管理器
		selector = SelectorProvider.provider().openSelector();
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		for (;;) {
			// 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
			while (selector.select() > 0) {
				Set<SelectionKey> readyKeys = selector.selectedKeys();
				Iterator<SelectionKey> i = readyKeys.iterator();
				while (i.hasNext()) {
					SelectionKey sk = i.next();
					i.remove();
					if (sk.isAcceptable()) {
						doAccept(sk);
					} else if (sk.isValid() && sk.isReadable()) {
						doRead(sk);
					} else if (sk.isValid() && sk.isWritable()) {
						doWrite(sk);
					}

				}
			}

		}
	}

	private void doRead(SelectionKey sk) {
		SocketChannel channel = (SocketChannel) sk.channel();
		ByteBuffer bb = ByteBuffer.allocate(8192);
		int len;
		try {
			len = channel.read(bb);
			if (len < 0) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// buff从写模式切换到读模式
		bb.flip();
		service.execute(new HandleMsg(sk, bb));

	}

	private void doAccept(SelectionKey sk) {
		ServerSocketChannel sever = (ServerSocketChannel) sk.channel();
		SocketChannel clientChannel;
		try {
			clientChannel = sever.accept();
			if (clientChannel != null) {
				clientChannel.configureBlocking(false);
				SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
				EchoClient echoClient = new EchoClient();
				clientKey.attach(echoClient);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void doWrite(SelectionKey sk) {
		SocketChannel channel = (SocketChannel) sk.channel();
		EchoClient echoClient = (EchoClient) sk.attachment();
		LinkedList<ByteBuffer> ouq = echoClient.getOutq();
		if (ouq.size() <= 0) {
			return;
		}
		ByteBuffer bb = ouq.getLast();
		try {
			byte[] data = bb.array();
			String msg = new String(data).trim();
			Request request = new Request(msg);
			Response response = new Response(channel);
			// =======业务逻辑
			String uri = request.getUri();
			if (uri == null || uri.equals("")) {
				return;
			}
			System.out.println("=========" + uri);
			if (FileUtils.isStatic(uri)) {
				response.writerFileByNIO(uri.substring(1));
			} else if (uri.indexOf(".action") != -1) {
				LoginServlet loginServlet = new LoginServlet();
				loginServlet.service(request, response);
			}
			bb.clear();
			ouq.removeLast();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("doWriter");
	}

	class EchoClient {
		private LinkedList<ByteBuffer> outq;

		EchoClient() {
			outq = new LinkedList<ByteBuffer>();
		}

		public LinkedList<ByteBuffer> getOutq() {
			return outq;
		}

		public void enqueue(ByteBuffer bb) {
			outq.addFirst(bb);
		}
	}

	class HandleMsg implements Runnable {
		SelectionKey sk;
		ByteBuffer bb;

		public HandleMsg(SelectionKey sk, ByteBuffer bb) {
			this.sk = sk;
			this.bb = bb;
			System.out.println("doRead");
		}

		@Override
		public void run() {
			EchoClient echoClient = (EchoClient) sk.attachment();
			echoClient.enqueue(bb);
			sk.interestOps(SelectionKey.OP_WRITE);
			selector.wakeup();
		}
	}

}

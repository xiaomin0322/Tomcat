package com.lx.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.Future;

import com.lx.util.FileUtils;

/**
 * 
 * @author lixiang
 *
 */
public class Response {
	private OutputStream out;
	private SocketChannel socketChannel;

	private AsynchronousSocketChannel asynchronousSocketChannel;

	public Response(OutputStream out) {
		this.out = out;
	}

	public Response(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public Response(AsynchronousSocketChannel asynchronousSocketChannel) {
		this.asynchronousSocketChannel = asynchronousSocketChannel;
	}

	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public AsynchronousSocketChannel getAsynchronousSocketChannel() {
		return asynchronousSocketChannel;
	}

	public void setAsynchronousSocketChannel(AsynchronousSocketChannel asynchronousSocketChannel) {
		this.asynchronousSocketChannel = asynchronousSocketChannel;
	}

	public void writerHtmlFile(String path) throws IOException {
		// 读取静态资源文件
		String htmlContent = FileUtils.getFileContent(path);
		out.write(htmlContent.getBytes());
		out.flush();
		out.close();
	}

	public void writerFile(String path) throws IOException {
		FileInputStream fis = new FileInputStream(path);
		byte[] buff = new byte[512];
		int len = 0;
		while ((len = fis.read(buff)) != -1) {
			out.write(buff, 0, len);
		}
		fis.close();
		out.flush();
		out.close();

	}

	public void outWrite(String message) {
		try {
			if (getSocketChannel() != null) {
				Charset cn = Charset.forName("GBK");
				ByteBuffer buffer = cn.encode(message);
				getSocketChannel().write(buffer);
				getSocketChannel().shutdownInput();
				getSocketChannel().close();
			} else if (getOut() != null) {
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getOut(), "GBK");
				outputStreamWriter.write(message);
				outputStreamWriter.close();
			} else {
				Charset cn = Charset.forName("GBK");
				ByteBuffer buffer = cn.encode(message);
				getAsynchronousSocketChannel().write(buffer);
				getAsynchronousSocketChannel().shutdownInput();
				getAsynchronousSocketChannel().close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writerFileByNIO(String path) throws IOException {
		FileInputStream fis = new FileInputStream(path);
		FileChannel fileChannel = fis.getChannel();
		ByteBuffer buf = ByteBuffer.allocate(2048);
		fileChannel.read(buf);
		buf.flip();
		// 将消息回送给客户端
		fileChannel.close();
		socketChannel.write(buf);
		socketChannel.shutdownInput();
		socketChannel.close();

	}

	public Future<Integer> writerFileByAIO(String path) throws IOException {
		FileInputStream fis = new FileInputStream(path);
		FileChannel fileChannel = fis.getChannel();
		ByteBuffer buf = ByteBuffer.allocate(2048);
		fileChannel.read(buf);
		buf.flip();
		fileChannel.close();
		return asynchronousSocketChannel.write(buf);
	}
}

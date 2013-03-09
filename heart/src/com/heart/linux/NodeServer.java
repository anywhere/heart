package com.heart.linux;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class NodeServer extends Thread {
	// 保存所有节点机器
	static HashMap<String, Node> hm = new HashMap<String, Node>();
	// 当前的连接数和工作线程数
	static int workThreadNum = 0;
	static int socketConnect = 0;
	Object hashLock = new Object();
	private ServerSocket serverSocket;
	// 服务器IP
	private static final String SERVER = "192.168.0.22";
	// 端口
	private static final int PORT = 60001;

	public void run() {
		// 绑定端口,并开始侦听用户的心跳包
		serverSocket = startListenUserReport(PORT);
		if (serverSocket == null) {
			System.out.println("创建ServerSocket失败！");
			return;
		}
		// 等待用户心跳包请求
		while (true) {
			Socket socket = null;
			try {
				socketConnect = socketConnect + 1;
				// 接收客户端的连接
				socket = serverSocket.accept();
				// 为该连接创建一个工作线程
				Thread workThread = new Thread(new Handler(socket));
				// 启动工作线程
				workThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建一个ServerSocket来侦听用户心跳包请求
	 * 
	 */
	public ServerSocket startListenUserReport(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket();
			if (!serverSocket.getReuseAddress()) {
				serverSocket.setReuseAddress(true);
			}
			serverSocket.bind(new InetSocketAddress(SERVER, port));
			System.out.println("开始在" + serverSocket.getLocalSocketAddress()
					+ "上侦听用户的心跳包请求！");
			return serverSocket;
		} catch (IOException e) {
			System.out.println("端口" + port + "已经被占用！");
			if (serverSocket != null) {
				if (!serverSocket.isClosed()) {
					try {
						serverSocket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return serverSocket;
	}

	// 工作线程类
	class Handler implements Runnable {
		private Socket socket;

		/**
		 * 构造函数，从调用者那里取得socket
		 * 
		 */
		public Handler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			Node node = null;
			try {
				workThreadNum = workThreadNum + 1;
				System.out.println("第" + workThreadNum + "个的连接:"
						+ socket.getInetAddress() + ":" + socket.getPort());
				while (true) {
					ObjectInputStream ois = new ObjectInputStream(
							new BufferedInputStream(socket.getInputStream()));
					node = (Node) ois.readObject();
					synchronized (hashLock) {
						if (hm.containsKey(node.getIp()))
							hm.remove(node.getIp());
						hm.put(node.getIp(), node);
					}
					System.out.println("节点ip：" + node.getIp());
					System.out.println("节点空闲cpu：" + node.getFree_cpu());
					System.out.println("节点硬盘总大小：" + node.getDf());
					System.out.println("节点硬盘已使用大小：" + node.getUsed_df());
					System.out.println("节点硬盘空闲大小：" + node.getUsed_df());
					System.out.println("节点内存大小：" + node.getRam());
					System.out.println("节点内存已使用大小：" + node.getUsed_ram());
					System.out.println("节点内存空闲大小：" + node.getFree_ram());
				}
			} catch (IOException e) {
				synchronized (hashLock) {
					hm.remove(node.getIp());
				}
				System.out.println("用户已经断开连接！");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						// 断开连接
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 查询一个用户是否在线
	 * 
	 */
	public boolean isAlive(String name) {
		synchronized (hashLock) {
			return hm.containsKey(name);
		}
	}

	public static void main(String arg[]) {
		NodeServer server = new NodeServer();
		System.out.println(".............启动服务器..................");
		server.start();
	}
}
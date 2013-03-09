package com.heart.linux;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class NodeServer extends Thread {
	// �������нڵ����
	static HashMap<String, Node> hm = new HashMap<String, Node>();
	// ��ǰ���������͹����߳���
	static int workThreadNum = 0;
	static int socketConnect = 0;
	Object hashLock = new Object();
	private ServerSocket serverSocket;
	// ������IP
	private static final String SERVER = "192.168.0.22";
	// �˿�
	private static final int PORT = 60001;

	public void run() {
		// �󶨶˿�,����ʼ�����û���������
		serverSocket = startListenUserReport(PORT);
		if (serverSocket == null) {
			System.out.println("����ServerSocketʧ�ܣ�");
			return;
		}
		// �ȴ��û�����������
		while (true) {
			Socket socket = null;
			try {
				socketConnect = socketConnect + 1;
				// ���տͻ��˵�����
				socket = serverSocket.accept();
				// Ϊ�����Ӵ���һ�������߳�
				Thread workThread = new Thread(new Handler(socket));
				// ���������߳�
				workThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ����һ��ServerSocket�������û�����������
	 * 
	 */
	public ServerSocket startListenUserReport(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket();
			if (!serverSocket.getReuseAddress()) {
				serverSocket.setReuseAddress(true);
			}
			serverSocket.bind(new InetSocketAddress(SERVER, port));
			System.out.println("��ʼ��" + serverSocket.getLocalSocketAddress()
					+ "�������û�������������");
			return serverSocket;
		} catch (IOException e) {
			System.out.println("�˿�" + port + "�Ѿ���ռ�ã�");
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

	// �����߳���
	class Handler implements Runnable {
		private Socket socket;

		/**
		 * ���캯�����ӵ���������ȡ��socket
		 * 
		 */
		public Handler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			Node node = null;
			try {
				workThreadNum = workThreadNum + 1;
				System.out.println("��" + workThreadNum + "��������:"
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
					System.out.println("�ڵ�ip��" + node.getIp());
					System.out.println("�ڵ����cpu��" + node.getFree_cpu());
					System.out.println("�ڵ�Ӳ���ܴ�С��" + node.getDf());
					System.out.println("�ڵ�Ӳ����ʹ�ô�С��" + node.getUsed_df());
					System.out.println("�ڵ�Ӳ�̿��д�С��" + node.getUsed_df());
					System.out.println("�ڵ��ڴ��С��" + node.getRam());
					System.out.println("�ڵ��ڴ���ʹ�ô�С��" + node.getUsed_ram());
					System.out.println("�ڵ��ڴ���д�С��" + node.getFree_ram());
				}
			} catch (IOException e) {
				synchronized (hashLock) {
					hm.remove(node.getIp());
				}
				System.out.println("�û��Ѿ��Ͽ����ӣ�");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						// �Ͽ�����
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * ��ѯһ���û��Ƿ�����
	 * 
	 */
	public boolean isAlive(String name) {
		synchronized (hashLock) {
			return hm.containsKey(name);
		}
	}

	public static void main(String arg[]) {
		NodeServer server = new NodeServer();
		System.out.println(".............����������..................");
		server.start();
	}
}
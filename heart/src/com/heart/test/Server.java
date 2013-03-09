package com.heart.test;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Server extends Thread {
	private ServerSocket server = null;
	Object obj = new Object();

	@Override
	public void run() {
		try {
			server = new ServerSocket(25535);
			while (true) {
				Socket client = server.accept();
				synchronized (obj) {
					new Thread(new Client(client)).start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** * �ͻ����߳� * @author USER * */
	class Client implements Runnable {
		Socket client;

		public Client(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {
				while (true) {
					ObjectInput in = new ObjectInputStream(
							client.getInputStream());
					Entity entity = (Entity) in.readObject();
					// System.out.println(entity.getName());
					// System.out.println(entity.getSex());
					System.out.println(entity.getName() + "" + entity.getSex());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** *��������main���� * @param args */
	public static void main(String[] args) {
		new Server().start();
	}
}
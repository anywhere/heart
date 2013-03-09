package com.heart.test;

public class Client extends Thread {
	@Override
	public void run() {
		try {
			while (true) {
				ClientSender.getInstance().send();
				synchronized (Client.class) {
					// this.wait(5000);
					Thread.sleep(2000);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** * ��������main���� * @param args */
	public static void main(String[] args) {
		Client client = new Client();
		client.start();
	}
}
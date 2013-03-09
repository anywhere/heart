package com.heart.linux;
public class NodeClient implements Runnable {
	private static NodeClient client = null;
	private boolean stoped = false;
	private int interval = 1000;

	private NodeClient() {
	}

	public static NodeClient getNodeClient() {
		if (client == null) {
			client = new NodeClient();
		}
		return client;
	}

	public void run() {
		while (!stoped) {
			try {
				NodeSender.send();
				synchronized (this) {
					this.wait(interval);
				}
			} catch (Exception e) {
			}
		}
	}

	public void destroy() {
		stoped = true;
		synchronized (this) {
			this.notify();
		}
	}

	public static void main(String[] args) {
		Thread t = new Thread(new NodeClient());
		t.start();
	}
}
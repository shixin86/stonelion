package com.xiaomi.stonelion.threads;

import java.util.Timer;
import java.util.TimerTask;

public class TimeOut extends Timer {
	public TimeOut(long timeOut) {
		super(true);// Daemon thread
		schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Time out.");
				System.exit(0);
			}
		}, timeOut);
	}
}

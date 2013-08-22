package com.xiaomi.stonelion.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SerialNumber {
	// volatile关键字是不要对number进行优化, 因为没有线程都会有一个本地栈来维护变量的副本
	private static volatile int number;

	// 为了证明java中的自增操作也不是线程安全的
	public static int nextSerialNumber() {
		return number++;
	}
}

public class SerialNumberChecker_3 extends Thread{ 
	private static List<Integer> list = Collections.synchronizedList(new ArrayList<Integer>());
	
	@Override
	public void run() {
		while(true){
			int serialNumber = SerialNumber.nextSerialNumber();
			if(list.contains(serialNumber)){
				System.out.println("Duplicate:" + serialNumber);
				System.exit(1);
			}
			list.add(serialNumber);
		}
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 20	; i++) {
			new SerialNumberChecker_3().start();
		}
		
		new TimeOut(5000);
		
	}
}

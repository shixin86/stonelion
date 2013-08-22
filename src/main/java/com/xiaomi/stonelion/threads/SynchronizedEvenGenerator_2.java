package com.xiaomi.stonelion.threads;

public class SynchronizedEvenGenerator_2 implements Invariant {
	private int i;

	public synchronized void next() {
		i++;
		i++;
	}

	public synchronized int getValue() {
		return i;
	}

	@Override
	public InvariantState getInvariantState() {
		// TODOAuto-generated method stub
		int val = getValue();
		if (val % 2 == 0)
			return new InvariantSuccess();
		else
			return new InvariantFailure(new Integer(val));
	}

	public static void main(String[] args) {
		SynchronizedEvenGenerator_2 evenGenerator_1 = new SynchronizedEvenGenerator_2();
		InvariantWatcher wather = new InvariantWatcher(evenGenerator_1, 5000);
		while (true)
			evenGenerator_1.next();
	}
}

package com.xiaomi.stonelion.threads;

public class EvenGenerator_1 implements Invariant {
	private int i;

	public void next() {
		i++;
		i++;
	}

	public int getValue() {
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
		EvenGenerator_1 evenGenerator_1 = new EvenGenerator_1();
		InvariantWatcher wather = new InvariantWatcher(evenGenerator_1, 5000);
		while (true)
			evenGenerator_1.next();
	}
}

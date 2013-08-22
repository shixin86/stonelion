package com.xiaomi.stonelion.threads;

public class InvariantWatcher extends Thread {
	private Invariant invariant;

	public InvariantWatcher(Invariant invariant, long timeout) {
		setDaemon(true);
		this.invariant = invariant;
		new TimeOut(timeout);
		start();
	}

	public void run() {
		while (true) {
			InvariantState state = this.invariant.getInvariantState();
			if (state instanceof InvariantFailure) {
				System.out.println("Invariant violated:"
						+ ((InvariantFailure) state).values);
				System.exit(1);
			}
		}
	}
}

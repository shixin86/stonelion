package com.xiaomi.stonelion.threads;

public class InvariantFailure implements InvariantState {
	public Object values;

	public InvariantFailure(Object value) {
		this.values = value;
	}
}
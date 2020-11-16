package com.todotask.env.impl;

import com.core.model.api.MutableState;

public interface StateTimerObserver {

	public void onStateChanged(MutableState state, boolean value);
	
}

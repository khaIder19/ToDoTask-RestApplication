package com.todotask.env.impl;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import com.core.model.api.MutableState;
import com.google.common.base.Supplier;

@ApplicationScoped
public class StateTimerService {

	private StateTimer timer;
	
	@Resource(mappedName = "java:jboss/ee/concurrency/scheduler/default")
	private ManagedScheduledExecutorService service;
	
	public StateTimerService() {		
	}
	
	public StateTimerService(StateTimer timer,ManagedScheduledExecutorService service) {
		this.service = service;
		this.timer = timer;
	}
	
	
	public void initTimer(long unit) {
		timer = new StateTimer(unit);
	}
	
	public boolean insertState(MutableState state) {
		if(timer == null) {
			throw new IllegalStateException();
		}
		return timer.insertState(state);
	}
	
	public void addObserver(StateTimerObserver observer) {
		if(timer == null)
			throw new IllegalStateException();
		timer.addObserver(observer);
	}
	
	public void startTimer(long delay,long period,TimeUnit unit,Supplier<Long> timeSupplier) {
		System.out.println("Timer Service Started with");
		if(timer == null)
			throw new IllegalStateException();
		service.scheduleAtFixedRate(new Runnable() {
			
		@Override
		public void run() {
				if(!timer.isEmpty()) {
					timer.onTick(timeSupplier.get());
				}
			}
		}, delay, period, unit);
	}
	
	
}

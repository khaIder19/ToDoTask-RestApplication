package com.todotask.rest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import org.apache.log4j.Logger;
import com.core.model.api.MutableState;
import com.core.model.impl.adjustable.dependent.states.DependencyState;
import com.google.common.base.Supplier;
import com.todotask.env.impl.StateTimerObserver;
import com.todotask.env.impl.StateTimerService;
import com.todotask.persistence.dao.StateDAO;

@ApplicationScoped
public class CDIbootListener {

	private static Logger log = Logger.getLogger(CDIbootListener.class);
	
	@Inject
	private StateDAO stateDao;
	
	@Inject
	private StateTimerService timer;
	
	private final long SECONDS_IN_A_DAY = 86400;
	
	public void onStartTimerService(@Observes(notifyObserver = Reception.ALWAYS) @Initialized(ApplicationScoped.class) Object  object) {
		timer.initTimer(SECONDS_IN_A_DAY);
		
		for(DependencyState dp : stateDao.getAll()) {
			timer.insertState(dp);
		}
		
		long nextMiddleNight = Instant.now().until(ZonedDateTime.of(LocalDate.now().plusDays(1),LocalTime.of(0,0),ZoneId.of("Z")),ChronoUnit.SECONDS);
		timer.startTimer(nextMiddleNight,1,TimeUnit.DAYS,new Supplier<Long>() {
			
			@Override
			public Long get() {
				return Instant.now().getEpochSecond();
			}
		});
		
		timer.addObserver(new StateTimerObserver() {
			
			@Override
			public void onStateChanged(MutableState state, boolean value) {
				if(state instanceof DependencyState) {
					stateDao.update((DependencyState)state);
				}
			}
		});
		
		
		
		log.info("Timer service initialized");
	}
}

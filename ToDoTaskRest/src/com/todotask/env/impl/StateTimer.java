package com.todotask.env.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.core.model.TimeRange;
import com.core.model.api.MutableState;
import com.core.model.impl.Side;

public class StateTimer {

	private List<StateTimerObserver> observerList;

    private long offSet = -1;

    private long unit;

    private NavigableMap<Long,List<MutableState>> incomingStates;

    private NavigableMap<Long,List<MutableState>> outgoingStates;
    

    public StateTimer(long unit) {
        this.unit = unit;
        observerList = new CopyOnWriteArrayList<>();
        incomingStates = new ConcurrentSkipListMap<>();
        outgoingStates = new ConcurrentSkipListMap<>();
    }


    public boolean insertState(MutableState state) {
        if(state.isExtinct() && !state.getStateValue())
            return false;
        
        if(state.getRange().getDuration() % unit != 0) {
        	return false;
        }
        
        long nextOffSet = offSet + unit;
        
        if(nextOffSet > unit) {
        	long nextOffSetDistanceFromState = state.getRange().getStart() - nextOffSet;
        	if(state.getRange().getStart() < nextOffSet || nextOffSetDistanceFromState % unit != 0) {
        		return false;
        	}
        }
        
        return insertState(state,incomingStates,true, Side.START);
    }

    private boolean insertState(MutableState state, NavigableMap<Long,List<MutableState>> map, boolean extinctInsertion, Side side) {

        boolean insertResult = false;

        if(!extinctInsertion) {
            return false;
        }

        List<MutableState> offSetIncomingList = map.get(state.getRange().getSide(side));

        if(offSetIncomingList != null) {
            offSetIncomingList.add(state);
        }else {

            offSetIncomingList = new ArrayList<MutableState>();
            offSetIncomingList.add(state);
            map.put(state.getRange().getSide(side),offSetIncomingList);
        }

        return insertResult;

    }

    public void addObserver(StateTimerObserver observer) {
        observerList.add(observer);
    }

    public void onTick(long offSet) {
        long assertOffSet = this.offSet + unit;
        if(this.offSet != -1) {
            if (assertOffSet < offSet || assertOffSet > assertOffSet)
                throw new java.lang.IllegalStateException();
        }

        onTick(offSet,incomingStates,outgoingStates,Side.START,true);

        onTick(offSet,outgoingStates,null,Side.END,false);

        this.offSet = offSet;
    }




    private void onTick(long offSet,NavigableMap<Long,List<MutableState>> incomingSource,NavigableMap<Long,List<MutableState>> outgoingSource,Side side,boolean toOut) {
        NavigableMap<Long,List<MutableState>> nextStates = incomingSource.subMap(offSet,true, offSet,true);

        boolean condition = false;

        switch (side) {
            case START:
                condition = true;
                break;
            case END:
                condition = false;
                break;
            default:
                break;
        }

        if(!nextStates.isEmpty()) {

            Iterator<MutableState> stateIterator = nextStates.get(offSet).iterator();
            while(stateIterator.hasNext()) {

                MutableState state = stateIterator.next();

                if(setStateCondition(state,condition)) {

                    if(toOut && outgoingSource != null) {
                        insertState(state, outgoingSource, true, TimeRange.getOpposite(side));
                    }

                }

            }

            incomingSource.remove(offSet);
        }
    }


    private boolean setStateCondition(MutableState state,boolean condition) {

        boolean result = state.setStateValue(condition);

        if(result) {

            for(StateTimerObserver observer : observerList) {
                observer.onStateChanged(state, state.getStateValue());
            }

        }

        return result;
    }
	
    public boolean isEmpty() {
    	return (incomingStates.isEmpty() && outgoingStates.isEmpty());
    }
	
}

package com.todotask.env.impl;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;


public abstract class BaseUserTransaction implements UserTransaction{


	private UserTransaction ut;
	
	public BaseUserTransaction(UserTransaction ut) {
		this.ut = ut;
	}
	
	public abstract void begin() throws NotSupportedException, SystemException;


	
	public abstract void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
			SecurityException, IllegalStateException, SystemException;

	
	public UserTransaction getTransaction() {
		return ut;
	}

	@Override
	public int getStatus() throws SystemException {
		return ut.getStatus();
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		ut.rollback();
	}
	
	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		ut.setRollbackOnly();
	}

	@Override
	public void setTransactionTimeout(int arg0) throws SystemException {
		ut.setTransactionTimeout(arg0);
	}
	
	
}

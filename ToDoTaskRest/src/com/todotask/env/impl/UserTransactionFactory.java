package com.todotask.env.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

@ApplicationScoped
public class UserTransactionFactory{

	
	@Dependent
	@Produces
	@TransactionTypeAttribute(TransactionType.JOIN)
	public UserTransaction getJoinTransaction(UserTransaction ut) {
		return new BaseUserTransaction(ut) {
			
			private boolean isJoined = false;
			
			@Override
			public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
					SecurityException, IllegalStateException, SystemException {
				if(isJoined) {
					return;
				}else {
					getTransaction().commit();
				}
			}
			
			@Override
			public void begin() throws NotSupportedException, SystemException {
				if(getStatus() == Status.STATUS_ACTIVE) {
					isJoined = true;
					return;
				}else {
					getTransaction().begin();
				}
			}
		};
	}
	
	@Dependent
	@TransactionTypeAttribute(TransactionType.DEF)
	@Produces
	public UserTransaction getNewTransaction(UserTransaction ut) {
		return new BaseUserTransaction(ut) {
			
			@Override
			public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
					SecurityException, IllegalStateException, SystemException {
				getTransaction().commit();
			}
			
			@Override
			public void begin() throws NotSupportedException, SystemException {
				getTransaction().begin();
			}
		};
	}
	
}

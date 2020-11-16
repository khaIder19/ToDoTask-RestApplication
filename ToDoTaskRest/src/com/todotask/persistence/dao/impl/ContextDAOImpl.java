package com.todotask.persistence.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import com.todotask.interceptors.ContextUpdate;
import com.todotask.interceptors.ContextUpdateInterceptor;
import com.todotask.model.context.Context;
import com.todotask.persistence.dao.ContextDAO;

@ApplicationScoped
public class ContextDAOImpl implements ContextDAO{

	@PersistenceContext(unitName = "todotaskPU")
	private EntityManager em;
	
	@Override
	public List<Context> getAll() {
		Query all = em.createQuery("FROM Context");
		return all.getResultList();
	}

	@Override
	public List<Context> getById(String... ids) {
		if(ids.length < 1) {
			return new ArrayList<Context>();
		}
		Query byUser = em.createQuery("SELECT c FROM Context c WHERE c.uid IN :inList ") ;
		byUser.setParameter("inList",Arrays.asList(ids));
		return byUser.getResultList();
	}

	@Override
	public List<Context> getByUser(String user_uid) {
		Query byUser = em.createQuery("SELECT c FROM Context c WHERE c.creatorId = :user") ;
		byUser.setParameter("user", user_uid);
		return byUser.getResultList();
	}

	
	@Override
	public boolean delete(Context ctx) {
		try {
			em.joinTransaction();
			em.remove(em.contains(ctx) ? ctx : em.merge(ctx));
		}catch(PersistenceException e) {
			em.getTransaction().setRollbackOnly();
			return false;
		}
			return true;
	}

	@ContextUpdate("CTX")
	@Interceptors(ContextUpdateInterceptor.class)
	@Override
	public boolean update(Context ctx) {
		try {
			em.joinTransaction();
			em.merge(ctx);
		}catch(PersistenceException e) {
			em.getTransaction().setRollbackOnly();
			return false;
		}
			return true;
	}

	
	@Override
	public boolean insert(Context ctx) {
		try {
			em.joinTransaction();
			em.persist(ctx);
		}catch(PersistenceException e) {
			em.getTransaction().setRollbackOnly();
			return false;
		}
			return true;
	}

}

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
import org.apache.log4j.Logger;
import com.todotask.interceptors.ContextUpdate;
import com.todotask.interceptors.ContextUpdateInterceptor;
import com.todotask.model.taskcontent.TaskContent;
import com.todotask.persistence.dao.TaskContentDAO;

@ApplicationScoped
public class TaskContentDAOImpl implements TaskContentDAO{

	private static Logger log = Logger.getLogger(TaskContentDAOImpl.class);
	
	
	@PersistenceContext(unitName = "todotaskPU")
	private EntityManager em;
	

	@Override
	public List<TaskContent> getAll() {
		Query all = em.createQuery("FROM TaskContent");
		return all.getResultList();
	}


	
	@Override
	public List<TaskContent> findByParent(String parent_uid) {
		Query all = em.createQuery("SELECT c FROM TaskContent c WHERE c.parent = :parentId");
		all.setParameter("parentId",parent_uid);
		return all.getResultList();
	}
	
	
	@Override
	public boolean delete(TaskContent... tc) {	
		try {
			em.joinTransaction();
			for(TaskContent task : tc) {
				em.remove(em.contains(task) ? task : em.merge(task));
			}
		}catch(PersistenceException e) {
			em.getTransaction().setRollbackOnly();
			return false;
		}

		return true;
	}


	@Override
	public boolean deleteByIds(String... ids) {
		try {
			em.joinTransaction();
			Query q = em.createQuery("DELETE TaskContent WHERE taskid IN :idlist");
			q.setParameter("idlist",Arrays.asList(ids));		
		}catch(PersistenceException e) {
			em.getTransaction().setRollbackOnly();
			return false;
		}

		return true;
	}
	
	@Override
	public List<TaskContent> getById(String... ids) {
		if(ids.length < 1) {
			return new ArrayList();
		}
		Query byUser = em.createQuery("SELECT c FROM TaskContent c WHERE c.taskid IN (:inList) ") ;
		byUser.setParameter("inList",Arrays.asList(ids));
		return byUser.getResultList();
	}
	
	@Override
	public List<TaskContent> findDependents(String dependency) {
		Query byUser = em.createQuery("SELECT c FROM TaskConstraint c WHERE c.dependencyUid = :depId ") ;
		byUser.setParameter("depId",dependency);
		return byUser.getResultList();
	}



	@Override
	public List<TaskContent> findDependencies(String dependent) {
		Query byUser = em.createQuery("SELECT c FROM TaskConstraint c WHERE c.dependentUid = :depId ") ;
		byUser.setParameter("depId",dependent);
		return byUser.getResultList();
	}
	
	@ContextUpdate("TASK")
	@Interceptors(ContextUpdateInterceptor.class)
	@Override
	public boolean update(TaskContent tc) {
		try {
			em.joinTransaction();
			em.merge(tc);		
		}catch(PersistenceException e) {
			em.getTransaction().setRollbackOnly();
			return false;
		}
		return true;
	}
	
	
	@Override
	public boolean insert(TaskContent tc) {
		try {
			em.joinTransaction();
			em.persist(tc);
		}catch(PersistenceException e) {
			em.getTransaction().setRollbackOnly();
			return false;
		}
		return true;
	}
	
}

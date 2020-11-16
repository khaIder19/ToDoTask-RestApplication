package com.todotask.persistence.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.hibernate.Session;
import com.todotask.data.TaskProvider;
import com.todotask.interceptors.ContextUpdate;
import com.todotask.persistence.dao.StateDAO;
import com.todotask.persistence.dao.model.StateLoadType;
import com.core.model.impl.Side;
import com.core.model.impl.adjustable.dependent.states.DependencyState;


@ApplicationScoped
public class StateDAOImpl implements StateDAO {

	@PersistenceContext(unitName = "todotaskPU")
	private EntityManager em;
	
	private Map<Long,DependencyState> map;
	
	public StateDAOImpl() {
		map = new ConcurrentHashMap<>();
	}
	
	@PostConstruct
	public void init() {		
		TypedQuery<DependencyState> byUser = em.createQuery("FROM DependencyState",DependencyState.class) ;
		List<DependencyState> list = byUser.getResultList();
		for(DependencyState e : list) {
			long id = TaskProvider.getDependencyStateId(e);
			e.setValidationRange(e.getBoundedRange());
			map.put(id,e);
		}
	}
	
	@PreDestroy
	public void store() {	
		for(Map.Entry<Long,DependencyState> e : map.entrySet()) {
			em.persist(e.getValue());
		}
	}
	
	
	@Override
	public DependencyState getById(Long id) {
		return map.get(id);
	}

	@Override
	public List<DependencyState> getAll() {
		List<DependencyState> list = new ArrayList<DependencyState>();
		for(Map.Entry<Long,DependencyState> e : map.entrySet()) {
			list.add(e.getValue());
		}
		return list;
	}

	@Override
	public List<DependencyState> find(Long[] ids) {
		List<DependencyState> list = new ArrayList<>();
		for(long id : ids) {
			list.add(map.get(id));
		}
		return list;
	}

	@Override
	public List<DependencyState> get(Long id, Side side, StateLoadType type) {
		return null;
	}

	
	@Override
	public Long insert(DependencyState state) {
		em.joinTransaction();
		Session s = (Session) em.getDelegate();
		Long id = (Long) s.save(state);
		map.put(id, state);
		return id;
	}

	@ContextUpdate
	@Override
	public boolean update(DependencyState... states) {
		/*
		try {
			for(DependencyState e : states) {
				em.merge(e);	
			}
		}catch(PersistenceException e) {
			em.getTransaction().setRollbackOnly();
			return false;
		}
		*/
		return true;
	}

	@Override
	public boolean delete(DependencyState state) {
			state.destroy();
			map.remove(Long.valueOf(TaskProvider.getDependencyStateId(state)));
			return true;
	}

}

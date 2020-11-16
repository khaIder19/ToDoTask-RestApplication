package com.todotask.persistence.dao.impl;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.log4j.Logger;
import com.todotask.interceptors.RequestUpdate;
import com.todotask.model.request.RequestEntity;
import com.todotask.model.request.RequestPayload;
import com.todotask.model.request.RequestState;
import com.todotask.model.request.RequestType;
import com.todotask.persistence.dao.RequestDAO;

@ApplicationScoped
public class RequestDAOImpl implements RequestDAO{

	private static Logger log = Logger.getLogger(RequestDAOImpl.class);
	
	private static Properties p;
	
	public static final String ENV_REQ_DAO_PROPS ="env.var.prop.reqdao";
	
	static {
		p = new Properties();
		try {
			p.load(new FileReader(System.getenv(ENV_REQ_DAO_PROPS)));
		} catch (Exception e) {
			log.error("Error on properties load");
		}
	}
	
	//private static final String REQUEST = p.getProperty("dao.request.entity");
	//private static final String REQUEST_DATA = p.getProperty("dao.request.requestdata");
	
	private static final String REQUEST_uid = p.getProperty("dao.reequest.entity.request_uid");
	private static final String REQUEST_from = p.getProperty("dao.reequest.entity.user_form_uid");
	private static final String REQUEST_to= p.getProperty("dao.reequest.entity.user_to_uid");
	private static final String REQUEST_at= p.getProperty("dao.reequest.entity.created_at");
	private static final String REQUEST_state= p.getProperty("dao.reequest.entity.state");
	
	//private static final String REQUEST_DATA_req_uid = p.getProperty("dao.reequest.requestdata.request_id_fk");
	private static final String REQUEST_DATA_type = p.getProperty("dao.reequest.requestdata.request_type");
	private static final String REQUEST_DATA_data = p.getProperty("dao.reequest.requestdata.request_args");
	
	
	private static final String INSERT_REQUEST = p.getProperty("dao.request.entity.sql.insert");
	private static final String INSERT_REQUEST_DATA = p.getProperty("dao.request.requestdata.sql.insert");
	
	private static final String REQUEST_QUERY_ALL = p.getProperty("");
	private static final String REQUEST_QUERY_BY_ID = p.getProperty("dao.request.entity.sql.query_by_uid");
	private static final String REQUEST_QUERY_BY_FROM = p.getProperty("dao.request.entity.sql.query_by_from");
	private static final String REQUEST_QUERY_BY_TO = p.getProperty("dao.request.entity.sql.query_by_to"); 
	private static final String REQUEST_QUERY_BY_FROM_TO = p.getProperty("dao.request.entity.sql.query_by_from_to");
	private static final String REQUEST_QUERY_BY_FROM_OR_TO = p.getProperty("dao.request.entity.sql.query_by_from_or_to");
	
	private static final String DELETE_BY_ID = p.getProperty("dao.request.entity.sql.delete");
	private static final String UPDATE_STATE = p.getProperty("dao.request.entity.sql.update");
	
	@Resource(lookup="java:/ToDoTaskDS")
	private DataSource ds;
	
	@Resource
	private UserTransaction ut;
	
	@Override
	public List<RequestEntity> getAll() {
		List<RequestEntity> result = new ArrayList<RequestEntity>();
		try(Connection c = ds.getConnection();PreparedStatement ps = c.prepareStatement(REQUEST_QUERY_ALL)){
				result = getList(ps);
			} catch (SQLException e) {
				
				log.error(e.getClass().toString(),e);
				
			}
		return result;
	}

	@Override
	public RequestEntity getById(String uid) {
		RequestEntity result = null;
		try(Connection c = ds.getConnection();PreparedStatement ps = c.prepareStatement(REQUEST_QUERY_BY_ID)){
					result = getRequestById(ps, uid);
			} catch (SQLException e) {
			
				log.error(e.getClass().toString(),e);
				
			}
		return result;
	}

	@Override
	public List<RequestEntity> getBy(String from, String to,RequestType type) {
		List<RequestEntity> result = new ArrayList<RequestEntity>();
		try(Connection c = ds.getConnection();PreparedStatement ps = switchCriteria(c,from, to,type)){
				if(ps != null) {
					result = getList(ps);
				}
			} catch (SQLException e) {
				
				log.error(e.getClass().toString(),e);
				
			}
		return result;
	}
	
	
	private List<RequestEntity> getList(PreparedStatement ps) throws SQLException{
		List<RequestEntity> result = new ArrayList<RequestEntity>();
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			result.add(fromResultSet(rs));
		}
		return result;
	}
	
	
	private PreparedStatement switchCriteria(Connection c,String from,String to,RequestType type) throws SQLException {
		PreparedStatement ps = null;
			
		if(from == null && to == null) {	
			return null;
		}
		
		if(from != null && to == null) {
			ps = from(c, from, type);
		}
		
		if(to != null && from == null) {
			ps = to(c, to, type);
		}
		
		if(to.equals(from)) {
			ps = fromOrTo(c, from, type);
		}else {
			ps = fromTo(c, from, to, type);
		}
		
		
		return ps;
	}

	
	private PreparedStatement from(Connection c,String from,RequestType type) throws SQLException {
		PreparedStatement ps;
		ps = c.prepareStatement(REQUEST_QUERY_BY_FROM);
		ps.setString(1, from);
		ps.setString(2, from);
		ps.setString(3,type.name());
		
		return ps;
	}
	
	private PreparedStatement to(Connection c,String to,RequestType type) throws SQLException {
		PreparedStatement ps;
		ps = c.prepareStatement(REQUEST_QUERY_BY_TO);
		ps.setString(1, to);
		ps.setString(2,to);
		ps.setString(3,type.name());
		System.out.println("request sql to");
		return ps;
	}

	private PreparedStatement fromTo(Connection c,String from,String to,RequestType type) throws SQLException {
		PreparedStatement ps;
		ps = c.prepareStatement(REQUEST_QUERY_BY_FROM_TO);
		ps.setString(1, from);
		ps.setString(2, to);
		ps.setString(3,type.name());
		
		return ps;
	}
	
	private PreparedStatement fromOrTo(Connection c,String from,RequestType type) throws SQLException {
		PreparedStatement ps;
		ps = c.prepareStatement(REQUEST_QUERY_BY_FROM_OR_TO);
		ps.setString(1, from);
		ps.setString(2, from);
		ps.setString(3,type.name());
		
		return ps;
	}
	
	
	@RequestUpdate
	@Override
	public boolean insert(RequestEntity req) {
		try(Connection c = ds.getConnection();
				PreparedStatement eps = c.prepareStatement(INSERT_REQUEST);
				PreparedStatement dps = c.prepareStatement(INSERT_REQUEST_DATA)){
			return (insertRequest(eps, req) & insertRequestData(dps, req));
		} catch (SQLException e) {
			try {
				if(ut.getStatus() == Status.STATUS_ACTIVE) {
					ut.rollback();
				}
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				
				log.error(e1.getClass().toString(),e1);
				
			}
			
			log.error(e.getClass().toString(),e);
		}
		return false;
	}

	
	@Override
	public boolean delete(RequestEntity req) {
		try(Connection c = ds.getConnection();PreparedStatement ps = c.prepareStatement(DELETE_BY_ID)){
			ps.setString(1, req.getRequest_uid());
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			try {
				if(ut.getStatus() == Status.STATUS_ACTIVE) {
					ut.rollback();
				}
			} catch (IllegalStateException | SecurityException | SystemException e1) {
			
				log.error(e1.getClass().toString(),e1);
				
			}
			
			log.error(e.getClass().toString(),e);
		}
		return false;
	}

	@RequestUpdate
	@Override
	public boolean update(RequestEntity req) {
		try(Connection c = ds.getConnection();PreparedStatement ps = c.prepareStatement(UPDATE_STATE)){
			ps.setString(1, req.getState().name());
			ps.setString(2, req.getRequest_uid());
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			try {
				if(ut.getStatus() == Status.STATUS_ACTIVE) {
					ut.rollback();
				}
			} catch (IllegalStateException | SecurityException | SystemException e1) {
			
				log.error(e1.getClass().toString(),e1);
			
			}
			
			log.error(e.getClass().toString(),e);
		}
		return false;
	}

	private boolean insertRequest(PreparedStatement ps,RequestEntity req) throws SQLException {
		ps.setString(1, req.getRequest_uid());
		ps.setString(2, req.getFrom_user_uid());
		ps.setString(3, req.getTo_user_uid());
		ps.setString(4, req.getState().name());
		return (ps.executeUpdate() == 1);
	}
	
	private boolean insertRequestData(PreparedStatement ps,RequestEntity req) throws SQLException {
		ps.setString(1, req.getRequest_uid());
		ps.setString(2, req.getData().getType());
		ps.setString(3, req.getData().getData());
		return (ps.executeUpdate() == 1);
	}
	
	private RequestEntity getRequestById(PreparedStatement ps,String uid) throws SQLException {
		ps.setString(1, uid);
		ps.setString(2, uid);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			return fromResultSet(rs);
		}else {
			return null;
		}
	}
	
	private RequestEntity fromResultSet(ResultSet rs) throws SQLException {
		String uid = rs.getString(REQUEST_uid);
		String from = rs.getString(REQUEST_from);
		String to = rs.getString(REQUEST_to);
		String state = rs.getString(REQUEST_state);
		String at = rs.getString(REQUEST_at);
		String type = rs.getString(REQUEST_DATA_type);
		String data = rs.getString(REQUEST_DATA_data);
		RequestEntity re = new RequestEntity(uid, from, to, RequestState.valueOf(state), new RequestPayload(RequestType.valueOf(type), data));
		re.setCreationTime(at);
		return re;
	}
}

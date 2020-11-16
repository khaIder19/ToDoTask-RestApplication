package com.todotask.persistence.dao.impl;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.log4j.Logger;
import com.todotask.interceptors.UserEvent;
import com.todotask.interceptors.UserEventInterceptor;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.UserDAO;

@ApplicationScoped
public class UserDAOImpl implements UserDAO {

	private static Logger log = Logger.getLogger(UserDAOImpl.class);
	
	private static Properties p;
	
	public static final String ENV_USER_DAO_PROPS ="env.var.prop.userdao";
	
	static {
		p = new Properties();
		try {
			p.load(new FileReader(System.getenv(ENV_USER_DAO_PROPS)));
		} catch (Exception e) {
			log.error("Error on properties load");
		}
	}
	
	public static final String USER_DATA = p.getProperty("dao.user.data");
	public static final String USER_IDENTITY = p.getProperty("dao.user.identity");	
	public static final String USER_id = p.getProperty("dao.user.data.user_id");
	public static final String USER_name = p.getProperty("dao.user.data.user_name");
	public static final String USER_email = p.getProperty("dao.user.data.user_email");
	public static final String USER_uid = p.getProperty("dao.user.identity.user_uid");
	public static final String USER_pass = p.getProperty("dao.user.data.user_pass");
	public static final String USER_salt = p.getProperty("dao.user.data.pass_salt");	
	public static final String USER_ALL_CONTENT_QUERY = p.getProperty("dao.user.sql.all_content");
	public static final String ALL_DATA_QUERY = p.getProperty("dao.user.sql.all_data");
	public static final String USER_DELETE = p.getProperty("dao.user.sql.user_delete");
	public static final String USER_BYEMAIL_QUERY = p.getProperty("dao.user.sql.data_query_by_email");
	public static final String USER_INSERT = p.getProperty("dao.user.sql.user_insert");
	public static final String USER_DATA_QUERY = p.getProperty("dao.user.sql.data_query_by_id");
	public static final String USER_IDENTITY_INSERT = p.getProperty("dao.user.sql.identity_insert");

	
	@Resource(lookup="java:/ToDoTaskDS")
	private DataSource ds;
	
	@Resource
	private UserTransaction ut;
	
		
	public UserDAOImpl() {
	
	}
	
	
	@Override
	public List<User> getAll() {
		List<User> list = null;
		try(Connection c = ds.getConnection();
				Statement ps = c.createStatement()) {
			list = getAllUsers(ps);
		} catch (SQLException e) {
			
			log.error(e.getClass().toString(),e);
				
		}
		return list;
	}

	@Override
	public  User getById(String id) {
		User user = null;
		try (Connection c = ds.getConnection();
				PreparedStatement ps = c.prepareStatement(USER_DATA_QUERY)){
			user = getUserById(ps, id);
		} catch (SQLException e) {
			
			log.error(e.getClass().toString(),e);
				
		}
		return user;
	}

	@Override
	public  User getOfEmail(String email) {
		User user = null;
		try (Connection c = ds.getConnection();
				PreparedStatement ps = c.prepareStatement(USER_BYEMAIL_QUERY)){
			user = getUserByEmail(ps, email);
		} catch (SQLException e) {
			
			log.error(e.getClass().toString(),e);
			
		}
		return user;
	}

	@Override
	public boolean delete(User user) {
		boolean result = false;
		try (PreparedStatement ps = ds.getConnection().prepareStatement(USER_DELETE)){
			result = deleteUser(ps, user.getUserName(), user.getUserEmail());
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
		return result;
	}

	@UserEvent()
	@Interceptors(UserEventInterceptor.class)
	@Override
	public boolean insert(User user) {
		try (Connection c = ds.getConnection();
				PreparedStatement userPS = c.prepareStatement(USER_INSERT);
				PreparedStatement idPS = c.prepareStatement(USER_IDENTITY_INSERT)){
			return (insertUserData(userPS,user) && insertUserIdentity(idPS, user.getUserId()));
		}catch (SQLException e) {
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
	
	private boolean insertUserData(PreparedStatement ps, User user) throws SQLException {
		ps.setString(1, user.getUserName());
		ps.setString(2, user.getUserEmail());
		ps.setString(3, user.getPass());
		ps.setString(4, user.getSalt());
		return (ps.executeUpdate() == 1);
	}
	
	private boolean insertUserIdentity(PreparedStatement ps,String uid) throws SQLException {
		ps.setString(1, uid);
		return (ps.executeUpdate() == 1);
	}
	
	
	private User getUserByEmail(PreparedStatement ps,String email) throws SQLException {
		ps.setString(1, email);
		ps.setString(2, email);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			return fromResultSet(rs);
		}else {
			return null;
		}
	}
	
	private List<User> getAllUsers(Statement s) throws SQLException{
		ResultSet rs = s.executeQuery(ALL_DATA_QUERY);
		List<User> list = new ArrayList<>();
		while(rs.next()) {
			list.add(fromResultSet(rs));
		}
		return list;
	}
	
	private User getUserById(PreparedStatement ps,String user_id) throws SQLException {
		ps.setString(1, user_id);
		ps.setString(2, user_id);
		ResultSet rs = ps.executeQuery();
		if(rs.next()) {
			return fromResultSet(rs);	
		}else {
			return null;
		}
	}
	
	private User fromResultSet(ResultSet rs) throws SQLException {
		String user_id = rs.getString(USER_uid);
		String user_name = rs.getString(USER_name);
		String user_email = rs.getString(USER_email);
		String user_pass =rs.getString(USER_pass);
		String user_salt = rs.getString(USER_salt);
		return new User(user_id,user_name,user_email,user_pass,user_salt);
	}
	
	private boolean deleteUser(PreparedStatement ps,String name,String email) throws SQLException {
		ps.setString(1,name);
		ps.setString(2,email);
		return (ps.executeUpdate() == 1);
	}
}

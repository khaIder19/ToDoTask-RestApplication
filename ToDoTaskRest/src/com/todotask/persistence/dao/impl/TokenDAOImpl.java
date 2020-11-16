package com.todotask.persistence.dao.impl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import com.todotask.persistence.dao.TokenDAO;

@ApplicationScoped
public class TokenDAOImpl implements TokenDAO{

	private static Logger log = Logger.getLogger(TokenDAOImpl.class);
	
	//user_uid(key) : user_token(value)
	private Map<String,String> tokenMap;
	
	@Resource(lookup="java:/ToDoTaskDS")
	private DataSource ds;
	
	private static Properties p;
	
	public static final String ENV_TOKEN_DAO_PROPS ="env.var.prop.tokendao";
	
	static {
		p = new Properties();
		try {
			p.load(new FileReader(System.getenv(ENV_TOKEN_DAO_PROPS)));
		} catch (Exception e) {
			log.error("Error on properties load");
		}
	}
	
	private static final String USER_uid = p.getProperty("dao.user.user_uid");
	private static final String USER_token = p.getProperty("dao.user.user_token");
	private static final String TOKEN_INSERT = p.getProperty("dao.user.sql.token_insert");
	private static final String TOKENS_ALL_QUERY = p.getProperty("dao.user.sql.token_all");
	
	public TokenDAOImpl() {
		tokenMap = new HashMap<String, String>();
	}
	
	@PostConstruct
	public void init() {
		try(Connection c = ds.getConnection();
				Statement s = c.createStatement()){
			ResultSet rs = s.executeQuery(TOKENS_ALL_QUERY);
			while(rs.next()) {
				tokenMap.put(rs.getString(USER_uid), rs.getString(USER_token));
			}
		} catch (SQLException e) {
			
			log.error(e.getClass().toString(),e);
			
		}
	}
	
	@Override
	public List<String> getAll() {
		List<String> list = new ArrayList<String>();
		
		for(Map.Entry<String, String> e : getMap()) {
			list.add(e.getValue());
		}
		
		return list;
	}

	@Override
	public String getById(String id) {
		return tokenMap.get(id);
	}

	@Override
	public boolean delete(String id) {
		return (tokenMap.remove(id)  != null );
	}

	@Override
	public boolean insert(String id,String token) {
		if(!tokenMap.containsKey(id)) {
			tokenMap.put(id, token);
		}
		return false;
	}

	@PreDestroy
	public void store() {
		try(Connection c = ds.getConnection();
				PreparedStatement ps = c.prepareStatement(TOKEN_INSERT)){
			Set<Map.Entry<String, String>> entrySet = getMap();
			for(Map.Entry<String,String> e : entrySet) {
				ps.setString(1, e.getKey());
				ps.setString(2, e.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException e) {
			
			log.error(e.getClass().toString(),e);
			
		}
	}
	
	private Set<Map.Entry<String,String>> getMap(){
		return tokenMap.entrySet();
	}
}

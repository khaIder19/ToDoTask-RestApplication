package com.todotask.model.context;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "userentry")
public class UserEntry implements Serializable{

	private static final long serialVersionUID = 2403627810357000565L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "user_uid")
	private String userUid;
	
	@Column(name = "user_permission")
	private String permission;
	
	private UserEntry() {
		
	}
	
	public UserEntry(String uid,ContextPermission perm) {
		this.userUid = uid;
		this.permission = perm.name();
	}

	public ContextPermission getPermission() {
		return ContextPermission.valueOf(permission);
	}

	public void setPermission(ContextPermission permission) {
		this.permission = permission.name();
	}

	public String getUserUid() {
		return userUid;
	}
	
}

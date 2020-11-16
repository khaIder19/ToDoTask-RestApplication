package com.todotask.model.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Entity
@Table(name = "contextcontent")
@SecondaryTable(name = "contextidentity")
public class Context implements Serializable{

	private static final long serialVersionUID = -7637031979938340166L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "context_content")
	private String content;
	
	@Column(name = "creator_uid")
	private String creatorId;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
	 @JoinTable(
	            name = "contextuser",
	            joinColumns = {@JoinColumn(name = "context_id", referencedColumnName = "id")},
	            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")}
	    )
	 @MapKey(name = "userUid")
	private Map<String,UserEntry> userMap;
	
	
	@OneToMany(cascade = CascadeType.ALL ,fetch = FetchType.EAGER,orphanRemoval = true)
	 @JoinTable(
	            name = "contextdelegation",
	            joinColumns = {
	            		@JoinColumn(name = "context_id", referencedColumnName = "id")},
	            inverseJoinColumns = {@JoinColumn(name = "delegation_id", referencedColumnName = "id")}
	    )
	 @MapKey(name = "resId")
	private Map<String,DelegationEntry> tasksMap;
	
	@Column(table = "contextidentity")
	private String uid;

	@Column(table = "contextidentity",updatable = false,insertable = false)
	private String created_at;
	
	private Context() {
		
	}
	
	public Context(String content,String creatorId,String uid) {
		this.content = content;
		this.creatorId = creatorId;
		this.uid = uid;
		userMap = new HashMap<>();
		insertUser(creatorId, ContextPermission.MAIN);
		tasksMap = new HashMap<>();
	}
	
	
	public boolean insertUser(String user,ContextPermission permission) {
		if(userMap.containsKey(user)) {
			return false;
		}
		userMap.put(user,new UserEntry(user,permission));
		return true;
	}
	
	public boolean removeUser(String user) {
		if(userMap.remove(user) != null) {
			return true;
		}else {
			return false;
		}
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public boolean insertTask(String task_uid,String creator) {
		return insertTask(task_uid,new DelegationEntry(task_uid,creator, creator, null));
	}
	
	public boolean insertTask(String task_uid,DelegationEntry delegation) {
		if(tasksMap.containsKey(task_uid)) {
			return false;
		}
		tasksMap.put(task_uid,delegation);
		return true;
	}
	
	public boolean removeTask(String task_uid) {
		if(tasksMap.remove(task_uid) != null) {
			return true;
		}else {
			return false;
		}
	}
	
	public boolean delegateTask(String task_uid,String delegated) {
		DelegationEntry currentDE =tasksMap.get(task_uid);
		if(currentDE.getTo().contentEquals(creatorId)) {
			if(userMap.containsKey(delegated)) {
				tasksMap.replace(task_uid,new DelegationEntry(task_uid,currentDE.getFrom(), delegated,null));
				return true;
			}
		}
		return false;
	}
	
	public Map<String,DelegationEntry> getTasks(){
		return tasksMap;
	}
	
	public DelegationEntry getTaskDataById(String id){
		return tasksMap.get(id);
	}
	
	public String getContent() {
		return content;
	}
	
	public String getCreatorId() {
		return creatorId;
	}
	
	public String getCreationTime() {
		return created_at;
	}
	
	
	public String getContextId() {
		return uid;
	}
	
	public void setCreationTime(String timestamp) {
		created_at = timestamp;
	}
	
	public Map<String,ContextPermission> getUserMap(){
		Map<String,ContextPermission> map = new HashMap<>();
		for(Map.Entry<String,UserEntry> e : userMap.entrySet()) {
			map.put(e.getKey(),e.getValue().getPermission());
		}
		return map;
	}
	
	public Map<String,UserEntry> getUserMapA(){
		return userMap;
	}
	
}

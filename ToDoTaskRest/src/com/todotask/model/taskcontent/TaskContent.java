package com.todotask.model.taskcontent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Entity
@SecondaryTable(name = "taskidentity")
@Table(name="taskcontent")
public class TaskContent implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="content")
	private String content;
	
	@Column(table = "taskidentity")
	private String taskid;
	
	@Enumerated(EnumType.STRING)
	private TaskType type;
	
	@Column(name = "parent_uid")
	private String parent;
	
	@Column(table = "taskidentity",insertable = false,updatable = false)
	private String created_at;
	
	
	/*@JoinTable(name = "taskconstraint",joinColumns = {
			@JoinColumn(name="dependent_uid",referencedColumnName = "id",nullable = false),
	},inverseJoinColumns = {@JoinColumn(name="id")})*/
	
	@OneToMany(cascade = {CascadeType.ALL},fetch = FetchType.EAGER,orphanRemoval = true)
	@JoinColumn(name="dependent_uid",nullable = false)
	private List<TaskConstraint> dependencies;
	
	
	private TaskContent() {
		
	}
	
	public TaskContent(String id,String content,TaskType type) {
		this.content = content;
		this.taskid = id;
		this.type = type;
		this.dependencies = new ArrayList<TaskConstraint>();
	}
		
	public void addDependecy(String constraint_uid,String dependency,TaskConstraintType type) {
		dependencies.add(new TaskConstraint(constraint_uid, dependency, type));
	}
	
	public boolean removeDependency(String constraint_uid) {
		Iterator<TaskConstraint> iterator = dependencies.iterator();
		while(iterator.hasNext()) {
			if(iterator.next().getConstraintUid().equals(constraint_uid)) {
				iterator.remove();
			}
		}			
		return true;
	}
	
	public List<TaskConstraint> getDependencies(){
		return dependencies;
	}
	
	public void setParent(String parent_uid) {
		this.parent = parent_uid;
	}
	
	public String getParent() {
		return parent;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTaskId() {
		return taskid;
	}

	public TaskType getType() {
		return type;
	}
	
	public void setCreationTime(String time) {
		this.created_at = time;
	}
	
	public String getCreationTime() {
		return created_at;
	}

	@Override
	public String toString() {
		return "TaskContent [content=" + content + ", id=" + taskid + ", type=" + type + ", parent=" + parent
				+ ", created_at=" + created_at + ", dependencies=" + dependencies + "]";
	}
	
	
	
}

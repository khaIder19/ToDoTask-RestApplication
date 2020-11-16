package com.todotask.model.context;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "delegationentry")
public class DelegationEntry implements Serializable {

	private static final long serialVersionUID = 4755184166463020614L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "res_uid")
	private String resId;
	
	@Column(name = "delegator_uid")
	private String from;
	
	@Column(name = "delegated_uid")
	private String to;
	
	@Column(updatable = false,insertable = false)
	private String delegated_at;
	
	private DelegationEntry() {
		
	}
	
	public DelegationEntry(String resId,String fromUserId,String toUserId,String delegatedAt) {
		this.resId = resId;
		this.from = fromUserId;
		this.to = toUserId;
		this.delegated_at = delegatedAt;
	}

	public String getDelegated_at() {
		return delegated_at;
	}


	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}
	
	public String getResId() {
		return resId;
	}
	
}

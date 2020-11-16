package com.todotask.json.task;

import com.todotask.json.request.DelegationItem;
import com.todotask.model.taskcontent.TaskType;

public class TaskItem{

	private String task_id;
	private String context_id;
	private String content;
	private StatusItem status;
	private String type;
	private String parent;
	private String created_at;
	private RangeItem range;
	private RangeItem valid_start;
	private RangeItem valid_end;
	private String creator;
	private DelegationItem delegated_to;
	
	public TaskItem(String task_id, String context_id, String content, StatusItem status, String type, String parent,
			String created_at, RangeItem range, RangeItem valid_start, RangeItem valid_end, String creator,
			DelegationItem delegated_to) {
		super();
		this.task_id = task_id;
		this.context_id = context_id;
		this.content = content;
		this.status = status;
		this.type = type;
		this.parent = parent;
		this.created_at = created_at;
		this.range = range;
		this.valid_start = valid_start;
		this.valid_end = valid_end;
		this.creator = creator;
		this.delegated_to = delegated_to;
	}
	
	public TaskItem() {
		
	}
	
	
	
	public String getTask_id() {
		return task_id;
	}

	public String getContext_id() {
		return context_id;
	}

	public String getContent() {
		return content;
	}

	public StatusItem getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}

	public String getParent() {
		return parent;
	}

	public String getCreated_at() {
		return created_at;
	}

	public RangeItem getRange() {
		return range;
	}

	public RangeItem getValid_start() {
		return valid_start;
	}

	public RangeItem getValid_end() {
		return valid_end;
	}

	public String getCreator() {
		return creator;
	}

	public DelegationItem getDelegated_to() {
		return delegated_to;
	}



	public static class Builder{
		
		private String task_id;
		private String context_id;
		private String content;
		private StatusItem status;
		private String type;
		private String parent;
		private String created_at;
		private RangeItem range;
		private RangeItem valid_start;
		private RangeItem valid_end;
		private String creator;
		private DelegationItem delegated_to;
		
		private Builder(String task_id,String ctx_id,String content) {
			this.task_id = task_id;
			this.context_id = ctx_id;
			this.content = content;
		}
		
		public static Builder create(String task_id,String ctx_id,String content) {
			return new Builder(task_id, ctx_id, content);
		}
		
		public Builder status(StatusItem item) {
			this.status = item;
			return this;
		}
		
		public Builder type(TaskType type) {
			this.type = type.name();
			return this;
		}

		public Builder parent(String parent) {
			this.parent = parent;
			return this;
		}

		public Builder createdAt(String time) {
			this.created_at = time;
			return this;
		}

		public Builder range(RangeItem r) {
			this.range = r;
			return this;
		}

		public Builder startValidRange(RangeItem r) {
			this.valid_start = r;
			return this;
		}

		public Builder endValidRange(RangeItem r) {
			this.valid_end = r;
			return this;
		}
		
		public Builder creator(String creator) {
			this.creator = creator;
			return this;
		}
		
		public Builder delegation(DelegationItem item) {
			this.delegated_to = item;
			return this;
		}
		
		
		public TaskItem build() {
			return new TaskItem(task_id,context_id,content,status,type,parent,created_at,range,valid_start,valid_end,creator,delegated_to);
		}
		
	}
	
}

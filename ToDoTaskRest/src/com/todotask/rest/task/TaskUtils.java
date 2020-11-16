package com.todotask.rest.task;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.todotask.model.context.DelegationEntry;
import com.todotask.model.taskcontent.TaskContent;

public class TaskUtils {

	public static Map<String,DelegationEntry> filterContextTasks(Map<String,DelegationEntry> toFilter,String to,String by){
			
	
		
		Stream<Entry<String, DelegationEntry>> stream = toFilter.entrySet().stream();
		
			return stream.filter(new Predicate<Entry<String,DelegationEntry>>() {
				public boolean test(Entry<String,DelegationEntry> t) {
					if(to != null) {
						return t.getValue().getTo().equals(to);
					}else {
						return true;
					}
				};
			})
				.filter(new Predicate<Entry<String,DelegationEntry>>() {
					public boolean test(Entry<String,DelegationEntry> t) {
						if(by != null) {
							return t.getValue().getFrom().equals(by);
						}else {
							return true;
						}
					};
				})
				.collect(Collectors.toMap(new Function<Entry<String, DelegationEntry>, String>() {
					public String apply(Map.Entry<String,DelegationEntry> t) {
						return t.getKey();
					};
				}, new Function<Entry<String, DelegationEntry>, DelegationEntry>() {
					public DelegationEntry apply(Map.Entry<String,DelegationEntry> t) {
						return t.getValue();
					};
				}));
				
	}
	
	public static List<TaskContent> filterTasksContent(List<TaskContent> toFilter,String type){	
		return toFilter.stream().filter(new Predicate<TaskContent>() {
			public boolean test(TaskContent t) {
				if(type != null) {
					return t.getType().equals(type);
				}else {
					return true;
				}
			};
		}).collect(Collectors.toList());	
	}
	
}

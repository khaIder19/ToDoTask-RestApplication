package com.todotask;

import com.core.model.impl.adjustable.dependent.constraint.api.Constraint;
import com.core.model.impl.adjustable.dependent.constraint.impl.ArgConstraint;
import javax.persistence.AttributeConverter;
import java.util.StringTokenizer;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ConstraintFieldConverter implements AttributeConverter<Constraint,String> {

	
	
    @Override
    public String convertToDatabaseColumn(Constraint constraint) {
    	String val = null;
    	
    	System.out.println("constraint instance"+constraint);
    	
        if(constraint instanceof ArgConstraint){
        	System.out.println("instanceof constrain converted");
            ArgConstraint cons = (ArgConstraint) constraint;
            StringBuilder builder = new StringBuilder(constraint.getClass().getName() + ":");
            for(int i = 0;i<cons.getArgs().length;i++){
                builder.append("arg"+i+"=").append(cons.getArgs()[i]).append(",");
            }
            val = builder.toString();
        }else {

        }
        System.out.println(val);
        return val;
    }

    @Override
    public Constraint convertToEntityAttribute(String s) {
    	if(s == null){
            return null;
        }

        String[] classParam =s.split(":");
        String params = null;
        String className = null;
        if(classParam.length == 2){
             className = classParam[0];
            params =classParam[1];
        }else if(classParam.length == 1){
            className = classParam[0];
        }
        ArgConstraint cons = null;
        Object[] args = null;
        if(params != null){
            StringTokenizer tokenizer = new StringTokenizer(params,",",false);
            args = new Object[tokenizer.countTokens()];
            int index = 0;
            while(tokenizer.hasMoreTokens()){
                args[index] = tokenizer.nextToken().substring(5);
                index++;
            }
        }

        try {
            cons = (ArgConstraint) Class.forName(className).newInstance();
            cons.setArgs(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cons;
    }
}

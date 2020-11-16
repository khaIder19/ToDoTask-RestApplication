package com.todotask;
import com.core.model.impl.adjustable.adjuster.api.RangeAdjuster;
import com.core.model.impl.adjustable.adjuster.impl.DynamicAdjuster;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class AdjusterFieldConverter implements AttributeConverter<RangeAdjuster,String> {

    @Override
    public String convertToDatabaseColumn(RangeAdjuster rangeAdjuster) {
        return rangeAdjuster.getClass().getName();
    }

    @Override
    public RangeAdjuster convertToEntityAttribute(String s) {
    	if(s == null) {
    		return new DynamicAdjuster();
    	}
        RangeAdjuster adjuster = null;
        try {
            adjuster = (RangeAdjuster) Class.forName(s).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return adjuster;
    }
}

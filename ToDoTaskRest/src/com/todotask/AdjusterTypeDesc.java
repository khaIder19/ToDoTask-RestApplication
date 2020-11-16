package com.todotask;
import com.core.model.impl.adjustable.adjuster.api.RangeAdjuster;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

public class AdjusterTypeDesc extends AbstractTypeDescriptor<RangeAdjuster> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3863769253161643321L;

	public AdjusterTypeDesc(){
        super(RangeAdjuster.class);
    }

    @Override
    public String toString(RangeAdjuster rangeAdjuster) {
        return rangeAdjuster.getClass().getName();
    }

    @Override
    public RangeAdjuster fromString(String s) {
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

    @Override
    public <X> X unwrap(RangeAdjuster rangeAdjuster, Class<X> aClass, WrapperOptions wrapperOptions) {
        return null;
    }

    @Override
    public <X> RangeAdjuster wrap(X x, WrapperOptions wrapperOptions) {
        return null;
    }
}

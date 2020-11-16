package com.todotask;
import com.core.model.impl.adjustable.dependent.constraint.api.Constraint;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

public class ConstraintTypeDesc extends AbstractTypeDescriptor<Constraint> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8041206148972892503L;

	public ConstraintTypeDesc(){
        super(Constraint.class);
    }

    @Override
    public String toString(Constraint constraint) {
        return new ConstraintFieldConverter().convertToDatabaseColumn(constraint);
    }

    @Override
    public Constraint fromString(String s) {
        return new ConstraintFieldConverter().convertToEntityAttribute(s);
    }

    @Override
    public <X> X unwrap(Constraint constraint, Class<X> aClass, WrapperOptions wrapperOptions) {
        return null;
    }

    @Override
    public <X> Constraint wrap(X x, WrapperOptions wrapperOptions) {
        return null;
    }
}

package com.basaki.example.postgres.jsonb.data.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.hibernate.usertype.DynamicParameterizedType;

/**
 * {@code JsonTypeDescriptor} is the descriptor for mapping of a JSON string to
 * a Java object and vice-versa.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/15/17
 */
public class JsonTypeDescriptor extends AbstractTypeDescriptor<Object> implements DynamicParameterizedType {

    public static final String CLASS_NAME = "className";

    private final ObjectMapper objectMapper;

    private Class<?> clazz;

    @Override
    public void setParameterValues(Properties parameters) {
        String className = parameters.getProperty(CLASS_NAME);

        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            throw new HibernateException("class not found", cnfe);
        }
    }

    protected JsonTypeDescriptor(final ObjectMapper objectMapper) {
        super(Object.class, new MutableMutabilityPlan<Object>() {
            @Override
            protected Object deepCopyNotNull(Object value) {
                try {
                    String str = objectMapper.writeValueAsString(value);
                    return objectMapper.readValue(str, value.getClass());
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to transform " + value + " to JSON object.");
                }
            }
        });
        this.objectMapper = objectMapper;
    }

    protected JsonTypeDescriptor() {
        this(new ObjectMapper());
    }

    @Override
    public String toString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to convert JSON object " + value + "to string.",
                    e);
        }
    }

    @Override
    public Object fromString(String string) {
        try {
            return objectMapper.readValue(string, clazz);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to convert String to " + clazz + e.getMessage(),
                    e);
        }
    }

    @Override
    public <X> X unwrap(Object value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }

        if (String.class.isAssignableFrom(type)) {
            return (X) toString(value);
        }

        if (Object.class.isAssignableFrom(type)) {
            try {
                return (X) objectMapper.readTree(toString(value));
            } catch (IOException e) {
                throw new RuntimeException("Failed to unwrap", e);
            }
        }

        throw unknownUnwrap(type);
    }

    @Override
    public <X> Object wrap(X value, WrapperOptions options) {
        if (value != null) {
            return fromString(value.toString());
        }

        return null;
    }
}
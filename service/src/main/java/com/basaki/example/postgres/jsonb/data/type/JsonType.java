package com.basaki.example.postgres.jsonb.data.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.usertype.DynamicParameterizedType;

/**
 * {@code JsonType} is a custom Hibernate type for processing JSON string
 * returned by a SQL call (native query). It turns a JSON string to a Java
 * bean.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/15/17
 */
public class JsonType extends AbstractSingleColumnStandardBasicType<Object>
        implements DynamicParameterizedType {

    public JsonType() {
        super(JsonSqlTypeDescriptor.INSTANCE, new JsonTypeDescriptor());
    }

    public JsonType(ObjectMapper objectMapper) {
        super(JsonSqlTypeDescriptor.INSTANCE,
                new JsonTypeDescriptor(objectMapper));
    }

    @Override
    public String getName() {
        return "json";
    }

    @Override
    public void setParameterValues(Properties parameters) {
        ((JsonTypeDescriptor) getJavaTypeDescriptor()).setParameterValues(
                parameters);
    }
}


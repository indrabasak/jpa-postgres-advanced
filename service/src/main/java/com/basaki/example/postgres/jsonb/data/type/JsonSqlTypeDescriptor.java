package com.basaki.example.postgres.jsonb.data.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * {@code JsonSqlTypeDescriptor} is the descriptor for the SQL side mapping of a
 * JSON string.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/15/17
 */
public class JsonSqlTypeDescriptor implements SqlTypeDescriptor {

    public static final JsonSqlTypeDescriptor INSTANCE =
            new JsonSqlTypeDescriptor();

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public boolean canBeRemapped() {
        return true;
    }

    @Override
    public <X> ValueBinder<X> getBinder(
            JavaTypeDescriptor<X> javaTypeDescriptor) {
        return new BasicBinder<X>(javaTypeDescriptor, this) {
            @Override
            protected void doBind(PreparedStatement st, X value, int index,
                    WrapperOptions options) throws SQLException {
                st.setString(index,
                        javaTypeDescriptor.unwrap(value, String.class,
                                options));
            }

            @Override
            protected void doBind(CallableStatement st, X value, String name,
                    WrapperOptions options) throws SQLException {
                st.setString(name,
                        javaTypeDescriptor.unwrap(value, String.class,
                                options));
            }
        };
    }

    @Override
    public <X> ValueExtractor<X> getExtractor(
            JavaTypeDescriptor<X> javaTypeDescriptor) {
        return new BasicExtractor<X>(javaTypeDescriptor, this) {

            @Override
            protected X doExtract(ResultSet rs, String name,
                    WrapperOptions options) throws SQLException {
                return javaTypeDescriptor.wrap(rs.getObject(name), options);
            }

            @Override
            protected X doExtract(CallableStatement statement, int index,
                    WrapperOptions options) throws SQLException {
                return javaTypeDescriptor.wrap(statement.getObject(index),
                        options);
            }

            @Override
            protected X doExtract(CallableStatement statement, String name,
                    WrapperOptions options) throws SQLException {
                return javaTypeDescriptor.wrap(statement.getObject(name),
                        options);
            }
        };
    }
}


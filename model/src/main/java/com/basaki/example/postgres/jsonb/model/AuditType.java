package com.basaki.example.postgres.jsonb.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * {@code AuditType} represents the type of database operation. Used for storing
 * audit records.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/16/17
 */
public enum AuditType {

    CREATE, UPDATE, DELETE;

    /**
     * Returns a <tt>AuditType<tt> enum based on string matching
     *
     * @param value string stored in database
     * @return a matching <tt>Genre</tt>
     */
    @JsonCreator
    public static AuditType fromValue(String value) {
        return valueOf(value.toUpperCase());
    }
}

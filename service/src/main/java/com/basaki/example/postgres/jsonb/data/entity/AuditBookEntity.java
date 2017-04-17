package com.basaki.example.postgres.jsonb.data.entity;

import com.basaki.example.postgres.jsonb.model.AuditBook;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * {@code AuditBookEntity} represents a row in the <code>Books</code> audit
 * books database table.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/16/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books", schema = "example_book_audit_schema")
public class AuditBookEntity implements Serializable {

    @EmbeddedId
    private AuditBookId id;

    @Column(name = "audit", nullable = false)
    @Type(type = "com.basaki.example.postgres.jsonb.data.usertype.JsonbUserType",
            parameters = {@Parameter(name = "className",
                    value = "com.basaki.example.postgres.jsonb.model.AuditBook")})
    private AuditBook audit;

    /**
     * {@code AuditBookId} represents the composite primary key for
     * {@code AuditBookEntity}
     */
    @Embeddable
    @Data
    public static class AuditBookId implements Serializable {

        @Column(name = "id", nullable = false)
        private UUID id;

        @Column(name = "made_at", nullable = false)
        private Date madeAt;
    }
}

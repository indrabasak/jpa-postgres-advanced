package com.basaki.example.postgres.jsonb.data.entity;

import com.basaki.example.postgres.jsonb.data.strategy.DirtyStateIdentifiable;
import com.basaki.example.postgres.jsonb.model.Book;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * {@code BookEntity} represents a row in the <code>Books</code> database table.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/16/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedStoredProcedureQuery(
        name = "book.setUser",
        procedureName = "example_book_schema.set_user",
        parameters = {
                @StoredProcedureParameter(name = "audit_user", mode = ParameterMode.IN, type = String.class)
        }
)
@NamedNativeQueries({
        @NamedNativeQuery(name = "book.search",
                query = "SELECT example_book_schema.to_json(book, 'match', " +
                        "example_book_schema.find_match(b, ?1)) AS books " +
                        "FROM example_book_schema.books b " +
                        "WHERE example_book_schema.search(b, ?1)")
})
@Entity
@Table(name = "books", schema = "example_book_schema")
public class BookEntity implements Serializable, DirtyStateIdentifiable {

    @Id
    @Column(name = "id", nullable = false)
    @Type(type = "pg-uuid")
    private UUID id;

    @Column(name = "book", nullable = false)
    @Type(type = "com.basaki.example.postgres.jsonb.data.usertype.JsonbUserType",
            parameters = {@Parameter(name = "className",
                    value = "com.basaki.example.postgres.jsonb.model.Book")})
    private Book book;

    @Transient
    private boolean dirty;
}

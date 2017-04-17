package com.basaki.example.postgres.jsonb.data.repository;

import com.basaki.example.postgres.jsonb.data.entity.BookEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * {@code BookRepository} exposes all CRUD operations on a data of type
 * {@code Book}.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/16/17
 */
@Repository
public interface BookRepository extends CustomJpaRepository<BookEntity, UUID> {

    @Query(value = "SELECT DISTINCT b.book->>'publisher' FROM example_book_schema.books b WHERE b.book->>'publisher' ILIKE CONCAT('%', ?1, '%')", nativeQuery = true)
    List<String> findDistinctPublisher(String publisher);

    @Procedure(name = "book.setUser")
    void setUser(@Param("audit_user") String user);

    @Modifying
    @Query(value = "SET search_path TO example_book_schema, public", nativeQuery = true)
    void setPath();
}

package com.basaki.example.postgres.jsonb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code Book} represents a book entity.
 * <p/>
 *
 * @author Indra Basak
 * @since 3/8/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {

    private UUID id;
    private String title;
    private Genre genre;
    private String publisher;
    private int star;
    private Author author;
    private String match;
}

package com.basaki.example.postgres.jsonb.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code AuditBook} represents a audit record for any write operation performed
 * on a book record.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/16/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditBook {

    private AuditType change;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date madeAt;

    private String madeBy;

    private Book state;
}



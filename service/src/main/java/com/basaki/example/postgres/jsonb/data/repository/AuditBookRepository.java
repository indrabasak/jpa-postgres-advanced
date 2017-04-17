package com.basaki.example.postgres.jsonb.data.repository;

import com.basaki.example.postgres.jsonb.data.entity.AuditBookEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * {@code AuditBookRepository} exposes read operations on a data of type
 * {@code AuditBook}.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/16/17
 */
@Repository
public interface AuditBookRepository extends
        CustomJpaRepository<AuditBookEntity, AuditBookEntity.AuditBookId> {

    List<AuditBookEntity> findByIdId(UUID id);
}

package com.basaki.example.postgres.jsonb.data.repository;

import java.io.Serializable;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

/**
 * {@code CustomSimpleJpaRepository} implements the {@code CustomJpaRepository}
 * and extends {@code SimpleJpaRepository}.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/15/17
 */
public class CustomSimpleJpaRepository<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements CustomJpaRepository<T, ID> {

    private final EntityManager entityManager;

    public CustomSimpleJpaRepository(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}

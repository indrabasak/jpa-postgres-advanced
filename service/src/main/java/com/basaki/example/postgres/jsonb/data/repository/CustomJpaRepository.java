package com.basaki.example.postgres.jsonb.data.repository;


import java.io.Serializable;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * {@code CustomJpaRepository} extends {@code JpaRepository} in order to expose
 * methods not exposed by {@code JpaRepository}.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/15/17
 */
@NoRepositoryBean
public interface CustomJpaRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    EntityManager getEntityManager();
}

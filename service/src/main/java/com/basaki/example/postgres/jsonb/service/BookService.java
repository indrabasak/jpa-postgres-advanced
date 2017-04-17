package com.basaki.example.postgres.jsonb.service;

import com.basaki.example.postgres.jsonb.data.entity.AuditBookEntity;
import com.basaki.example.postgres.jsonb.data.entity.BookEntity;
import com.basaki.example.postgres.jsonb.data.repository.AuditBookRepository;
import com.basaki.example.postgres.jsonb.data.repository.BookRepository;
import com.basaki.example.postgres.jsonb.data.type.JsonType;
import com.basaki.example.postgres.jsonb.data.type.JsonTypeDescriptor;
import com.basaki.example.postgres.jsonb.error.DataNotFoundException;
import com.basaki.example.postgres.jsonb.model.AuditBook;
import com.basaki.example.postgres.jsonb.model.Book;
import com.basaki.example.postgres.jsonb.model.BookRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.dozer.Mapper;
import org.hibernate.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * {@code BookService} service provides data access service for {@code Book}.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/16/17
 */
@Service
@Slf4j
public class BookService {

    private final BookRepository repo;

    private final AuditBookRepository auditRepo;

    private final Mapper mapper;

    private final ObjectMapper objectMapper;

    private JsonType bookJsontype;

    @Autowired
    public BookService(BookRepository repo, AuditBookRepository auditRepo,
            Mapper mapper, ObjectMapper objectMapper) {
        this.repo = repo;
        this.auditRepo = auditRepo;
        this.mapper = mapper;
        this.objectMapper = objectMapper;

        Properties props = new Properties();
        props.setProperty(JsonTypeDescriptor.CLASS_NAME,
                "com.basaki.example.postgres.jsonb.model.Book");
        bookJsontype = new JsonType(objectMapper);
        bookJsontype.setParameterValues(props);
    }

    @Transactional
    public Book create(BookRequest request, String user) {
        validate(request);

        UUID id = UUID.randomUUID();
        Book book = mapper.map(request, Book.class);
        book.setId(id);
        BookEntity entity = new BookEntity();
        entity.setId(id);
        entity.setBook(book);

        repo.setUser(user);
        entity = repo.save(entity);

        book = entity.getBook();

        log.info("Created book with id " + book.getId());

        return book;
    }

    public Book read(UUID id) {
        BookEntity entity = repo.findOne(id);

        if (entity == null) {
            throw new DataNotFoundException(
                    "Book with id " + id + " not found!");
        }

        Book book = entity.getBook();

        return book;
    }

    @Transactional
    public List<Book> readAll(String searchQuery) {
        @SuppressWarnings("JpaQueryApiInspection")
        Query query = repo.getEntityManager().createNamedQuery(
                "book.search").setParameter(1, searchQuery);
        query.unwrap(SQLQuery.class).addScalar("books", bookJsontype);

        @SuppressWarnings("unchecked")
        List<Book> books = query.getResultList();

        return books;
    }

    @Transactional
    public Book update(UUID id, BookRequest request, String user) {
        BookEntity entity = repo.findOne(id);

        if (entity == null) {
            throw new DataNotFoundException(
                    "Book with id " + id + " not found!");
        }

        validate(request);
        Book book = mapper.map(request, Book.class);
        book.setId(id);
        entity.setBook(book);
        entity.setDirty(true);

        repo.setUser(user);
        entity = repo.save(entity);

        book = entity.getBook();

        log.info("Updated book with id " + book.getId());

        return book;
    }

    @Transactional
    public void delete(UUID id, String user) {
        try {
            repo.delete(id);
        } catch (Exception e) {
            throw new DataNotFoundException(
                    "Book with id " + id + " not found!");
        }
    }

    @Transactional
    public void deleteAll(String user) {
        repo.deleteAll();
    }

    public List<String> getPublisher(String publisher) {
        return repo.findDistinctPublisher(publisher);
    }

    public List<AuditBook> readAudits(UUID id) {
        List<AuditBookEntity> entities = auditRepo.findByIdId(id);

        if (entities == null || entities.size() == 0) {
            throw new DataNotFoundException(
                    "No audits for for book with id " + id + "!");
        }

        return entities.stream().map(
                e -> e.getAudit()).collect(
                Collectors.toList());
    }

    private void validate(BookRequest request) {
        Assert.notNull(request.getTitle(), "Title should not be null.");
        Assert.notNull(request.getGenre(), "Genre should not be null.");
        Assert.notNull(request.getPublisher(), "Publisher should not be null.");
        Assert.notNull(request.getAuthor(), "Author should not be null.");
        Assert.state((request.getStar() > 0 && request.getStar() <= 5),
                "Star should be between 1 and 5");
    }

    private List<Book> map(List<BookEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            throw new DataNotFoundException(
                    "No books found with the search criteria!");
        }

        return entities.stream().map(
                r -> mapper.map(r, Book.class)).collect(
                Collectors.toList());
    }
}

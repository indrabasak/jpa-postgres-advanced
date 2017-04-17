package com.basaki.example.postgres.jsonb.controller;

import com.basaki.example.postgres.jsonb.model.AuditBook;
import com.basaki.example.postgres.jsonb.model.Book;
import com.basaki.example.postgres.jsonb.model.BookRequest;
import com.basaki.example.postgres.jsonb.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code BookController} is the spring REST controller for book API. Exposes
 * all CRUD operations on book.
 * <p/>
 *
 * @author Indra Basak
 * @since 4/16/17
 */
@SuppressWarnings("MVCPathVariableInspection")
@RestController
@Slf4j
@Api(value = "Book API",
        description = "Book API",
        produces = "application/json", tags = {"API"})
public class BookController {

    private static final String BOOK_URL = "/books";

    private static final String BOOK_BY_ID_URL = BOOK_URL + "/{id}";

    private static final String PUBLISHER_URL = BOOK_URL + "/publishers";

    private static final String BOOK_AUDITS_BY_ID_URL =
            BOOK_BY_ID_URL + "/audits";


    private BookService service;

    @Autowired
    public BookController(BookService service) {
        this.service = service;
    }

    @ApiOperation(
            value = "Creates a book.",
            notes = "Requires a book title, genre, publisher, star, and author.",
            response = Book.class)
    @ApiResponses({
            @ApiResponse(code = 201, response = Book.class,
                    message = "Book created successfully")})
    @RequestMapping(method = RequestMethod.POST, value = BOOK_URL,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(
            @ApiParam(value = "Audit user", required = true)
            @RequestHeader("user") String user,
            @RequestBody BookRequest request) {
        return service.create(request, user);
    }

    @ApiOperation(
            value = "Retrieves a book by ID.",
            notes = "Requires a book identifier",
            response = Book.class)
    @RequestMapping(method = RequestMethod.GET, value = BOOK_BY_ID_URL,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public Book read(
            @ApiParam(value = "Book ID", required = true)
            @PathVariable("id") UUID id) {
        return service.read(id);
    }

    @ApiOperation(
            value = "Retrieves all the books associated with the search string.",
            notes = "In absence of any parameter, it will return all the books.",
            response = Book.class, responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, value = BOOK_URL,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public List<Book> readAll(
            @ApiParam(value = "Search string to search a book. Returns all books if empty.")
            @RequestParam(value = "q", required = false) String searchQuery) {
        return service.readAll(searchQuery);
    }

    @ApiOperation(value = "Updates a book.", response = Book.class)
    @ApiResponses({
            @ApiResponse(code = 201, response = Book.class,
                    message = "Updated a book created successfully")})
    @RequestMapping(method = RequestMethod.PUT, value = BOOK_BY_ID_URL,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Book update(
            @ApiParam(value = "Audit user", required = true)
            @RequestHeader("user") String user,
            @ApiParam(value = "Book ID", required = true)
            @PathVariable("id") UUID id,
            @RequestBody BookRequest request) {
        return service.update(id, request, user);
    }

    @ApiOperation(value = "Deletes a book by ID.")
    @RequestMapping(method = RequestMethod.DELETE, value = BOOK_BY_ID_URL)
    @ResponseBody
    public void delete(
            @ApiParam(value = "Audit user", required = true)
            @RequestHeader("user") String user,
            @ApiParam(value = "Book ID", required = true)
            @PathVariable("id") UUID id) {
        service.delete(id, user);
    }

    @ApiOperation(value = "Deletes all books.")
    @RequestMapping(method = RequestMethod.DELETE, value = BOOK_URL)
    @ResponseBody
    public void deleteAll(
            @ApiParam(value = "Audit user", required = true)
            @RequestHeader("user") String user) {
        service.deleteAll(user);
    }

    @ApiOperation(
            value = "Retrieves a list of distinct publisher based on partial publisher name search.",
            notes = "Requires a partial publisher name",
            response = String.class, responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, value = PUBLISHER_URL,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public List<String> getPublisher(
            @ApiParam(value = "partial publisher name", required = true)
            @RequestParam(value = "q") String publisher) {
        return service.getPublisher(publisher);
    }

    @ApiOperation(
            value = "Retrieves audits for a book by ID.",
            response = AuditBook.class, responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, value = BOOK_AUDITS_BY_ID_URL,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public List<AuditBook> readAudits(
            @ApiParam(value = "Book ID", required = true)
            @PathVariable("id") UUID id) {
        return service.readAudits(id);
    }
}

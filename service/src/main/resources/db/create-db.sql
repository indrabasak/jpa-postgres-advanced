DROP SCHEMA IF EXISTS example_book_schema CASCADE;

DROP SCHEMA IF EXISTS example_book_audit_schema CASCADE;

DROP ROLE IF EXISTS owner_example_jpa;

CREATE ROLE owner_example_jpa WITH
NOLOGIN
NOSUPERUSER
INHERIT
NOCREATEDB
NOCREATEROLE
NOREPLICATION;

GRANT owner_example_jpa TO postgres;

CREATE SCHEMA example_book_schema AUTHORIZATION owner_example_jpa;

GRANT ALL ON SCHEMA example_book_schema TO owner_example_jpa;

CREATE SCHEMA example_book_audit_schema AUTHORIZATION owner_example_jpa;

GRANT ALL ON SCHEMA example_book_audit_schema TO owner_example_jpa;

-- **********************************************
-- example_book_audit_schema database creation
-- **********************************************
CREATE TYPE example_book_audit_schema.operation AS ENUM (
    'CREATE',
    'UPDATE',
    'DELETE'
);

ALTER TYPE  example_book_audit_schema.operation OWNER TO owner_example_jpa;

CREATE TABLE example_book_audit_schema.books (
    id          UUID         NOT NULL,
    audit       JSONB        NOT NULL,
    made_at     TIMESTAMPTZ  NOT NULL DEFAULT statement_timestamp(),
    PRIMARY KEY (id, made_at)
);

ALTER TABLE example_book_audit_schema.books OWNER to owner_example_jpa;

CREATE OR REPLACE FUNCTION example_book_audit_schema.json_time(
    ts TIMESTAMPTZ DEFAULT NOW()
) RETURNS TEXT LANGUAGE SQL IMMUTABLE STRICT AS $$
    SELECT to_char($1 AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.US"Z"');
$$;

CREATE OR REPLACE FUNCTION example_book_audit_schema.create_audit(
    op  example_book_audit_schema.operation,
    book JSONB DEFAULT '{}'
) RETURNS JSONB STABLE STRICT LANGUAGE PLPGSQL AS $$
DECLARE
    audit JSONB;
BEGIN
    audit := json_build_object(
        'change',  op,
        'state',   book,
        'madeBy',  current_setting('book.audit_user'),
        'madeAt',  example_book_audit_schema.json_time(statement_timestamp())
    );
    RETURN audit;
END;
$$;

REVOKE EXECUTE ON FUNCTION example_book_audit_schema.create_audit(example_book_audit_schema.operation, jsonb) FROM public;
GRANT EXECUTE ON FUNCTION example_book_audit_schema.create_audit(example_book_audit_schema.operation, jsonb) TO owner_example_jpa;

-- **********************************************
-- example_book_schema database creation
-- **********************************************

-- Create functions
CREATE OR REPLACE FUNCTION example_book_schema.validate_book(
    book JSONB
) RETURNS BOOLEAN IMMUTABLE LANGUAGE PLPGSQL AS $$
DECLARE
    errors   TEXT[] := '{}';
    attributes TEXT[] := '{id, title, genre, publisher, author, star}';
    genres TEXT[];
    attr    TEXT;
BEGIN
    IF book IS NULL THEN RETURN FALSE; END IF;

    IF jsonb_typeof(book) <> 'object' THEN
        RAISE check_violation USING
            MESSAGE = 'Book is not a JSON object!';
    END IF;

    -- Check all required attributes
    FOREACH attr IN ARRAY attributes LOOP
        IF (book->>attr) IS NULL THEN
            errors := errors || format('Attribute “%s” must not be null', attr);
        END IF;
    END LOOP;

    -- Check 'genre' is valid
    attr := book->>'genre';
    genres := (SELECT enum_range(NULL::example_book_schema.genre)::TEXT);
    IF attr IS NOT NULL AND
        attr != ALL(genres)
    THEN
        errors := errors || format('Attribute “genre” is not a valid type "%s"', array_to_string(genres, '”, “'));
    END IF;

    -- Check 'star' is valid
    attr := book->>'star';
    IF attr IS NOT NULL AND
        jsonb_typeof(book->'star') <> 'number' AND
        attr::int NOT BETWEEN 1 AND 5
    THEN
        errors := errors || format('Attribute “star” must be an integer between 1 and 5', attr);
    END IF;

    -- Check for errors
    IF array_length(errors, 1) > 0 THEN
        RAISE check_violation USING
            MESSAGE = 'Book document violates check constraint',
            DETAIL = array_to_string(errors, E'\n');
    END IF;

    RETURN TRUE;
END;
$$;

REVOKE EXECUTE ON FUNCTION example_book_schema.validate_book(JSONB) FROM public;
GRANT EXECUTE ON FUNCTION example_book_schema.validate_book(JSONB) TO owner_example_jpa;

CREATE OR REPLACE FUNCTION example_book_schema.audit_book_insert(
) RETURNS TRIGGER SECURITY DEFINER LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO example_book_audit_schema.books VALUES (
        NEW.id, example_book_audit_schema.create_audit('CREATE', NEW.book));
    RETURN NEW;
END;
$$;

REVOKE EXECUTE ON FUNCTION example_book_schema.audit_book_insert() FROM public;
GRANT EXECUTE ON FUNCTION example_book_schema.audit_book_insert() TO owner_example_jpa;

CREATE OR REPLACE FUNCTION example_book_schema.audit_book_update(
) RETURNS TRIGGER SECURITY DEFINER LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO example_book_audit_schema.books VALUES (
        NEW.id, example_book_audit_schema.create_audit('UPDATE', NEW.book));
    RETURN NEW;
END;
$$;

REVOKE EXECUTE ON FUNCTION example_book_schema.audit_book_update() FROM public;
GRANT EXECUTE ON FUNCTION example_book_schema.audit_book_update() TO owner_example_jpa;

CREATE OR REPLACE FUNCTION example_book_schema.audit_book_delete(
) RETURNS TRIGGER SECURITY DEFINER LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO example_book_audit_schema.books VALUES (
        OLD.id, example_book_audit_schema.create_audit('DELETE'));
    RETURN OLD;
END;
$$;

REVOKE EXECUTE ON FUNCTION example_book_schema.audit_book_delete() FROM public;
GRANT EXECUTE ON FUNCTION example_book_schema.audit_book_delete() TO owner_example_jpa;

CREATE OR REPLACE FUNCTION example_book_schema.set_user(
    audit_user TEXT
) RETURNS BOOLEAN STABLE LANGUAGE SQL AS $$
    SELECT set_config('book.audit_user', audit_user, true);
    SELECT TRUE;
$$;

REVOKE EXECUTE ON FUNCTION example_book_schema.set_user(text) FROM public;
GRANT EXECUTE ON FUNCTION example_book_schema.set_user(text) TO owner_example_jpa;

CREATE TYPE example_book_schema.genre AS ENUM (
    'DRAMA',
    'ROMANCE',
    'GUIDE',
    'TRAVEL');

ALTER TYPE example_book_schema.genre OWNER TO owner_example_jpa;

CREATE TABLE example_book_schema.books
(
  id      UUID PRIMARY KEY CHECK(id = (book->>'id')::UUID),
  book    JSONB NOT NULL CHECK (example_book_schema.validate_book(book))
);

ALTER TABLE example_book_schema.books OWNER to owner_example_jpa;

-- Create audit triggers
CREATE TRIGGER audit_book_insert AFTER INSERT ON example_book_schema.books
   FOR EACH ROW EXECUTE PROCEDURE example_book_schema.audit_book_insert();

CREATE TRIGGER audit_book_update AFTER UPDATE ON example_book_schema.books
   FOR EACH ROW EXECUTE PROCEDURE example_book_schema.audit_book_update();

CREATE TRIGGER audit_book_delete AFTER DELETE ON example_book_schema.books
   FOR EACH ROW EXECUTE PROCEDURE example_book_schema.audit_book_delete();

CREATE OR REPLACE FUNCTION example_book_schema.extract_fields(
    book JSONB
) RETURNS TEXT LANGUAGE SQL IMMUTABLE STRICT AS $$
    SELECT array_to_string(ARRAY[book->>'title', ' | ' ||
        (book->>'publisher'), ' | ' ||
        (book->'author'->>'firstName'), ' | ' ||
        (book->'author'->>'lastName')], '');
$$;

REVOKE EXECUTE ON FUNCTION example_book_schema.extract_fields(JSONB) FROM public;
GRANT EXECUTE ON FUNCTION example_book_schema.extract_fields(JSONB) TO owner_example_jpa;

CREATE OR REPLACE FUNCTION example_book_schema.search(
    tuple    example_book_schema.books,
    substr   TEXT
) RETURNS BOOLEAN LANGUAGE SQL IMMUTABLE STRICT AS $$
    SELECT LOWER(example_book_schema.extract_fields(tuple.book)) ~ LOWER('***=' || substr);
$$;

REVOKE EXECUTE ON FUNCTION example_book_schema.search(example_book_schema.books, TEXT) FROM public;
GRANT EXECUTE ON FUNCTION example_book_schema.search(example_book_schema.books, TEXT) TO owner_example_jpa;

CREATE OR REPLACE FUNCTION example_book_schema.find_match(
    tuple    example_book_schema.books,
    substr   TEXT
) RETURNS TEXT LANGUAGE SQL IMMUTABLE AS $$
    SELECT regexp_replace(example_book_schema.extract_fields(tuple.book),
        COALESCE('***=' || substr, '\Z(?=.)'), '{\&}', 'gi');

$$;

REVOKE EXECUTE ON FUNCTION example_book_schema.find_match(example_book_schema.books, TEXT) FROM public;
GRANT EXECUTE ON FUNCTION example_book_schema.find_match(example_book_schema.books, TEXT) TO owner_example_jpa;

CREATE OR REPLACE FUNCTION example_book_schema.to_json(
    target JSONB,
    key    TEXT,
    value  ANYELEMENT
) RETURNS JSONB LANGUAGE PLPGSQL IMMUTABLE AS $$
DECLARE
    string TEXT := target::TEXT;
BEGIN
    RETURN format('%s,%s:%s}', substr(string, 1, length(string)-1), to_json(key), COALESCE(to_json(value), 'null'));
END;
$$;

REVOKE EXECUTE ON FUNCTION example_book_schema.to_json(JSONB, TEXT, ANYELEMENT) FROM public;
GRANT EXECUTE ON FUNCTION example_book_schema.to_json(JSONB, TEXT, ANYELEMENT) TO owner_example_jpa;

COMMIT;

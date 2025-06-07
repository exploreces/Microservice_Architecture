package com.epam.library.proxyclass;

import com.epam.library.dto.BooksRequest;
import com.epam.library.dto.BooksResponse;
import com.epam.library.services.BooksClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BooksClientFallback implements BooksClient {

    @Override
    public ResponseEntity<List<BooksResponse>> getAllBooks() {
        return ResponseEntity.ok(Collections.singletonList(
                new BooksResponse(0L, "Default Book", "Default Publisher", "Default Author")
        ));
    }

    @Override
    public ResponseEntity<BooksResponse> getBookById(Long id) {
        return ResponseEntity.ok(new BooksResponse(0L, "Default Book", "Default Publisher", "Default Author"));
    }

    @Override
    public ResponseEntity<BooksResponse> saveBook(BooksRequest booksRequest) {
        return ResponseEntity.ok(new BooksResponse(0L, "Saved Fallback Book", "Fallback Publisher", "Fallback Author"));
    }

    @Override
    public ResponseEntity<BooksResponse> updateBook(Long id, BooksRequest booksRequest) {
        return ResponseEntity.ok(new BooksResponse(0L, "Updated Fallback Book", "Fallback Publisher", "Fallback Author"));
    }

    @Override
    public ResponseEntity<Void> deleteBook(Long id) {
        return ResponseEntity.ok().build();
    }
}


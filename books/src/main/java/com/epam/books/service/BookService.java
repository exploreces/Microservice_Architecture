package com.epam.books.service;

import com.epam.books.dto.request.BooksRequest;
import com.epam.books.dto.response.BooksResponse;
import com.epam.books.entity.Book;
import com.epam.books.exception.InvalidDataException;
import com.epam.books.exception.ResourceNotFoundException;
import com.epam.books.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private BookRepository bookRepository;
    private ObjectMapper objectMapper;

    @Autowired
    public BookService(BookRepository bookRepository, ObjectMapper objectMapper) {
        this.bookRepository = bookRepository;
        this.objectMapper = objectMapper;
    }

    public List<BooksResponse> getAllBooks() {
        logger.info("Fetching all books");
        List<Book> books = bookRepository.findAll();
        if (books.isEmpty()) {
            logger.warn("The Books list is Empty");
            throw new ResourceNotFoundException("The Books list is Empty");
        }
        logger.info("Retrieved {} books", books.size());
        return books.stream().map(book -> objectMapper.convertValue(book, BooksResponse.class))
                .toList();
    }

    public BooksResponse getBookById(Long id) {
        logger.info("Fetching book with id: {}", id);
        Book book = bookRepository.findById(id).orElseThrow(() -> {
            logger.error("Book with id {} not found", id);
            return new InvalidDataException("The id does not exist");
        });
        logger.info("Retrieved book: {}", book);
        return objectMapper.convertValue(book, BooksResponse.class);
    }

    public BooksResponse saveBook(BooksRequest booksRequest) {
        logger.info("Saving new book: {}", booksRequest);
        if (booksRequest.getName().isEmpty()) {
            logger.error("Attempt to save book with empty name");
            throw new InvalidDataException("The fields must be filled");
        }
        if (bookRepository.existsByName(booksRequest.getName())) {
            logger.error("Attempt to save book with existing name: {}", booksRequest.getName());
            throw new InvalidDataException("The book with the same name exists");
        }
        Book book = objectMapper.convertValue(booksRequest, Book.class);
        book = bookRepository.save(book);
        logger.info("Book saved successfully with id: {}", book.getId());
        return objectMapper.convertValue(book, BooksResponse.class);
    }

    public void delete(Long id) {
        logger.info("Deleting book with id: {}", id);
        Book book = bookRepository.findById(id).orElseThrow(() -> {
            logger.error("Attempt to delete non-existent book with id: {}", id);
            return new InvalidDataException("This Id does not exist");
        });
        bookRepository.delete(book);
        logger.info("Book deleted successfully");
    }

    public BooksResponse update(Long id, BooksRequest booksRequest) {
        logger.info("Updating book with id: {}", id);
        Book book = bookRepository.findById(id).orElseThrow(() -> {
            logger.error("Attempt to update non-existent book with id: {}", id);
            return new InvalidDataException("Please provide correct id as this id does not exist");
        });

        book.setName(booksRequest.getName());
        book.setAuthor(booksRequest.getAuthor());
        book.setPublisher(booksRequest.getPublisher());
        book = bookRepository.save(book);
        logger.info("Book updated successfully: {}", book);
        return objectMapper.convertValue(book, BooksResponse.class);
    }
}
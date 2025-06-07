package com.epam.library.services;

import com.epam.library.dto.BooksResponse;
import com.epam.library.dto.LibraryResponse;
import com.epam.library.entity.Library;
import com.epam.library.exceptions.InvalidDataException;
import com.epam.library.exceptions.ResourceNotFoundException;
import com.epam.library.repository.LibraryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibraryService {

    private static final Logger logger = LoggerFactory.getLogger(LibraryService.class);

    @Autowired
    private ObjectMapper objectMapper;

    private final BooksClient bookClient;
    private final UserClient userClient;
    private final LibraryRepository libraryRepository;

    @Autowired
    public LibraryService(BooksClient bookClient, UserClient userClient, LibraryRepository libraryRepository) {
        this.bookClient = bookClient;
        this.userClient = userClient;
        this.libraryRepository = libraryRepository;
    }

    public LibraryResponse issueBooks(String username, Long bookId) {
        logger.info("Issuing book with id {} to user {}", bookId, username);

        try {
            userClient.getByName(username).getBody();
        } catch (FeignException.FeignClientException e) {
            logger.error("User {} not found", username);
            throw new ResourceNotFoundException("The User does not exist");
        }

        try {
            bookClient.getBookById(bookId).getBody();
        } catch (FeignException.FeignClientException e) {
            logger.error("Book with id {} not found", bookId);
            throw new ResourceNotFoundException("The BookId does not exist");
        }

        long count = libraryRepository.countByUsername(username);
        if (count >= 3) {
            logger.warn("User {} already has 3 books issued", username);
            throw new InvalidDataException("User already has 3 books issued.");
        }

        List<Long> bookIssued = libraryRepository.findAllBookIdsByUsername(username);
        if(bookIssued.contains(bookId)){
            logger.warn("Book {} is already issued to user {}", bookId, username);
            throw new InvalidDataException("The bookId is already Issued");
        }
        Library library = new Library();
        library.setUsername(username);
        library.setBookId(bookId);
        libraryRepository.save(library);

        List<Long> bookIds = libraryRepository.findAllBookIdsByUsername(username);

        LibraryResponse response = new LibraryResponse();
        response.setUsername(username);
        response.setBookId(bookIds);
        logger.info("Book {} successfully issued to user {}", bookId, username);
        return response;
    }

    public List<BooksResponse> getIssuedBooks(String username) {
        logger.info("Fetching issued books for user {}", username);

        try {
            userClient.getByName(username).getBody();
        } catch (FeignException.FeignClientException e) {
            logger.error("User {} not found", username);
            throw new ResourceNotFoundException("The User does not exist");
        }

        List<Long> bookIds = libraryRepository.findAllBookIdsByUsername(username);

        if (bookIds.isEmpty()) {
            logger.warn("No books issued to user {}", username);
            throw new ResourceNotFoundException("No books issued to this user.");
        }

        logger.info("Retrieved {} books issued to user {}", bookIds.size(), username);
        return bookIds.stream()
                .map(b -> bookClient.getBookById(b).getBody())
                .toList();
    }

    public void deleteBook(Long id) {
        logger.info("Deleting book with id {}", id);
        try {
            libraryRepository.deleteByBookId(id);
            bookClient.deleteBook(id);
        } catch (FeignException.FeignClientException e) {
            logger.error("Book with id {} not found", id);
            throw new ResourceNotFoundException("The book does not exist");
        }

        bookClient.deleteBook(id);
        logger.info("Book with id {} successfully deleted", id);
    }

    public void deleteUser(String username) {
        logger.info("Deleting user {}", username);
        try {
            libraryRepository.deleteByUsername(username);
            userClient.deleteUser(username);
        } catch (FeignException.FeignClientException e) {
            logger.error("User {} not found", username);
            throw new ResourceNotFoundException("The User does not exist");
        }

        userClient.deleteUser(username);
        logger.info("User {} successfully deleted", username);
    }

    public void releaseBook(String username, Long id) {
        logger.info("Releasing book {} from user {}", id, username);
        libraryRepository.deleteByBookIdAndUsername(id, username);
        logger.info("Book {} successfully released from user {}", id, username);
    }
}
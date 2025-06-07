package com.epam.users.services;

import com.epam.users.Exceptions.InvalidRequestException;
import com.epam.users.Exceptions.ResourceNotFoundException;
import com.epam.users.dto.UserRequest;
import com.epam.users.dto.UserResponse;
import com.epam.users.dto.UserUpdateRequest;
import com.epam.users.entity.User;
import com.epam.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            logger.warn("No users found in the database");
            throw new ResourceNotFoundException("Currently No users are present");
        }
        logger.info("Retrieved {} users", users.size());
        return users.stream().map(user -> objectMapper.convertValue(user, UserResponse.class)).toList();
    }

    public UserResponse getByName(String username) {
        logger.info("Fetching user by name: {}", username);
        User user = userRepository.findByName(username).orElseThrow(
                () -> {
                    logger.error("User not found with username: {}", username);
                    return new InvalidRequestException("The username does not exist in database");
                }
        );
        logger.info("User found: {}", user);
        return objectMapper.convertValue(user, UserResponse.class);
    }

    public UserResponse saveUser(UserRequest userRequest) {
        logger.info("Saving new user: {}", userRequest);
        User user = objectMapper.convertValue(userRequest, User.class);

        userRepository.findByEmail(userRequest.getEmail()).ifPresent(existingUser -> {
            logger.error("Attempt to save user with existing email: {}", userRequest.getEmail());
            throw new InvalidRequestException(
                    "A user already exists with the email: " + userRequest.getEmail()
            );
        });
        user = userRepository.save(user);
        logger.info("User saved successfully with id: {}", user.getUsername());
        return objectMapper.convertValue(user, UserResponse.class);
    }

    public void deleteUser(String username) {
        logger.info("Deleting user with username: {}", username);
        User user = userRepository.findByName(username).orElseThrow(() -> {
            logger.error("Attempt to delete non-existent user with username: {}", username);
            return new InvalidRequestException("the username does not exist");
        });
        userRepository.delete(user);
        logger.info("User deleted successfully");
    }

    public UserResponse update(String username, UserUpdateRequest userRequest) {
        logger.info("Updating user with username: {}", username);
        User existUser = userRepository.findByName(username).orElseThrow(
                () -> {
                    logger.error("Attempt to update non-existent user with username: {}", username);
                    return new InvalidRequestException("The user does not exist  " + username);
                }
        );
        if (userRequest.getEmail() == null || userRequest.getEmail().isEmpty()) {
            logger.error("Attempt to update user with empty email");
            throw new InvalidRequestException("The email is empty");
        }
        if (userRequest.getName() == null || userRequest.getName().isEmpty()) {
            logger.error("Attempt to update user with empty username");
            throw new InvalidRequestException("The userName is empty");
        }
        existUser.setEmail(userRequest.getEmail());
        existUser.setName(userRequest.getName());
        existUser = userRepository.save(existUser);
        logger.info("User updated successfully: {}", existUser);
        return objectMapper.convertValue(existUser, UserResponse.class);
    }
}
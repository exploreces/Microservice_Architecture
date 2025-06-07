package com.epam.library.proxyclass;

import com.epam.library.dto.UserRequest;
import com.epam.library.dto.UserResponse;
import com.epam.library.dto.UserUpdateRequest;
import com.epam.library.services.UserClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(Collections.singletonList(
                new UserResponse("default_user", "Default User", "default@example.com")
        ));
    }

    @Override
    public ResponseEntity<UserResponse> getByName(String username) {
        return ResponseEntity.ok(new UserResponse("default_user", "Default User", "default@example.com"));
    }

    @Override
    public ResponseEntity<UserResponse> saveUser(UserRequest userRequest) {
        return ResponseEntity.ok(new UserResponse("saved_fallback", "Saved Fallback", "saved@example.com"));
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(String username, UserUpdateRequest userRequest) {
        return ResponseEntity.ok(new UserResponse("updated_fallback", "Updated Fallback", "updated@example.com"));
    }

    @Override
    public ResponseEntity<Void> deleteUser(String username) {
        return ResponseEntity.ok().build();
    }
}


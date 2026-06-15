package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.service.JwtService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // POST /api/users/register
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());  // raw password — will be hashed in service

        User saved = userService.register(user);
        return ResponseEntity.ok(saved);
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> getUsers(@PathVariable Long id) {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // PUT /api/users/{id}/goals
    @PutMapping("/{id}/goals")
    public ResponseEntity<User> updateGoals(
            @PathVariable Long id,
            @RequestParam Integer calorieGoal,
            @RequestParam Integer proteinGoal) {
        User updated = userService.updateGoals(id, calorieGoal, proteinGoal);
        return ResponseEntity.ok(updated);
    }
    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());
        String token = jwtService.generateToken(user.getId(), user.getEmail());

        LoginResponse response = new LoginResponse(
                token, user.getId(), user.getName(), user.getEmail());

        return ResponseEntity.ok(response);
    }
}
package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "users")  // "user" is a reserved word in PostgreSQL — always use "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    private Integer calorieGoal;
    private Integer proteinGoal;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Recipe> recipes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();  // auto set when saved
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getCalorieGoal() { return calorieGoal; }
    public void setCalorieGoal(Integer calorieGoal) { this.calorieGoal = calorieGoal; }

    public Integer getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(Integer proteinGoal) { this.proteinGoal = proteinGoal; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<Recipe> getRecipes() { return recipes; }
    public void setRecipes(List<Recipe> recipes) { this.recipes = recipes; }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", calorieGoal=" + calorieGoal +
                ", proteinGoal=" + proteinGoal +
                '}';
    }
}
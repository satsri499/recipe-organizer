package com.example.demo.repository;


import com.example.demo.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // All tags for a recipe
    List<Tag> findByRecipeId(Long recipeId);

    // Find recipes by tag name
    List<Tag> findByNameIgnoreCase(String name);
}

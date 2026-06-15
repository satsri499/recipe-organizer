package com.example.demo.repository;

import com.example.demo.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    // All collections for a user
    List<Collection> findByUserId(Long userId);

    // All recipes in a specific collection
    List<Collection> findByUserIdAndName(Long userId, String name);
}

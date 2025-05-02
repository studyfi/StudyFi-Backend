package com.studyfi.userandgroup.user.repo;

import com.studyfi.userandgroup.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {  // Changed Long to Integer
    User findByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.groups WHERE u.id = :userId")
    Optional<User> findByIdWithGroups(@Param("userId") Integer userId);

}

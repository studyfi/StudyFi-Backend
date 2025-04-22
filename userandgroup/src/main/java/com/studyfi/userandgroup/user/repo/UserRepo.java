package com.studyfi.userandgroup.user.repo;

import com.studyfi.userandgroup.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Integer> {  // Changed Long to Integer
    User findByEmail(String email);

    // Custom query method to find User by reset token
    User findByResetToken(String resetToken);
}

package com.studyfi.userandgroup.group.repo;

import com.studyfi.userandgroup.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepo extends JpaRepository<Group, Integer> {  // Changed Long to Integer
}

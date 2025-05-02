package com.studyfi.userandgroup.group.repo;

import com.studyfi.userandgroup.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepo extends JpaRepository<Group, Integer> {

    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.users WHERE g.id = :groupId")
    Optional<Group> findByIdWithUsers(@Param("groupId") Integer groupId); // Added method declaration
}

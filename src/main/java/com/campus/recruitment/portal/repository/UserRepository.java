package com.campus.recruitment.portal.repository;

import com.campus.recruitment.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);
    List<User> findByRoleAndApproved(User.Role role, boolean approved);
    List<User> findByRoleAndEnabled(User.Role role, boolean enabled);
}

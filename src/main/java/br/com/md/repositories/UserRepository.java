package br.com.md.repositories;

import br.com.md.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByActive(boolean active);

    List<User> findAllByRole(String role);

    List<User> findAllByActiveAndRole(boolean active, String role);

    List<User> findByEmailContainingIgnoreCase(String email);


    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.active = true")
    List<User> findAllActiveAdmins();

    Page<User> findAllByActive(boolean active, Pageable pageable);

    Page<User> findAllByActiveAndRole(boolean active, String role, Pageable pageable);
}

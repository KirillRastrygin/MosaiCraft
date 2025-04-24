package ru.rsreu.MosaiCraft.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.MosaiCraft.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

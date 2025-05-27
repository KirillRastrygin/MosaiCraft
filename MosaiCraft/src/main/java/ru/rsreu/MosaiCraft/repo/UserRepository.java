package ru.rsreu.MosaiCraft.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.MosaiCraft.entities.database.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

}

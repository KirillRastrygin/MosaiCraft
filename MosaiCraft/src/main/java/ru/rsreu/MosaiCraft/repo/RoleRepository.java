package ru.rsreu.MosaiCraft.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.MosaiCraft.entities.database.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}

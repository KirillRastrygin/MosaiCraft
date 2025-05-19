package ru.rsreu.MosaiCraft.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.MosaiCraft.entities.database.Template;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByUserId(Long userId);
    List<Template> findByIsCommonTrue();
}

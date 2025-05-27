package ru.rsreu.MosaiCraft.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.MosaiCraft.entities.database.Mosaic;

public interface MosaicRepository extends JpaRepository<Mosaic, Long> {
}

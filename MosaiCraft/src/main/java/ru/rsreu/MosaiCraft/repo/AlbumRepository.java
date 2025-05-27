package ru.rsreu.MosaiCraft.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.MosaiCraft.entities.database.Album;

public interface AlbumRepository  extends JpaRepository<Album, Long> {
}

package ru.rsreu.MosaiCraft.entities.database;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "mosaics")
public class Mosaic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "mosaics")
    @Cascade({ org.hibernate.annotations.CascadeType.ALL })
    private List<Album> albums;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String name;

    @Column(name = "mosaic_path")
    private String mosaicPath;

    @CreationTimestamp  // Автоматически при создании
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Mosaic(String name, String mosaicPath) {
        this.name = name;
        this.mosaicPath = mosaicPath;
    }

}

package ru.rsreu.MosaiCraft.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "albums")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String name;

    @CreationTimestamp  // Автоматически при создании
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "album", orphanRemoval = false)
    private List<Mosaic> mosaics;

    public void addMosaic(Mosaic mosaic) {
        mosaic.setAlbum(this);
        mosaics.add(mosaic);
    }

    public void removeMosaic(Mosaic mosaic) {
        mosaics.remove(mosaic);
    }

}

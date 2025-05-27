package ru.rsreu.MosaiCraft.entities.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String name;

    @CreationTimestamp  // Автоматически при создании
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Mosaic> mosaics;

    public Album(String name) {
        this.name = name;
        mosaics = new ArrayList<>();
    }

    public void addMosaic(Mosaic mosaic) {
        mosaics.add(mosaic);
    }

    public void removeMosaic(Mosaic mosaic) {
        mosaics.remove(mosaic);
    }



}

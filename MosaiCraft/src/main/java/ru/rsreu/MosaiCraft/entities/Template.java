package ru.rsreu.MosaiCraft.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "templates")
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String name;

    @Column(name = "is_common")
    private boolean isCommon;

    @CreationTimestamp  // Автоматически при создании
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    public Template(String name, boolean isCommon) {
        this.name = name;
        this.isCommon = isCommon;
    }

    public void addImage(Image image) {
        image.setTemplate(this);
        images.add(image);
    }

    public void removeImage(Image image) {
        images.remove(image);
    }


}

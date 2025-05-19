package ru.rsreu.MosaiCraft.entities.database;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    @Column(name = "image_path")
    private String imagePath;

    @Column
    private Double red;

    @Column
    private Double green;

    @Column
    private Double blue;

    public Image(Template template, String imagePath, Double red, Double green, Double blue) {
        this.template = template;
        this.imagePath = imagePath;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

}

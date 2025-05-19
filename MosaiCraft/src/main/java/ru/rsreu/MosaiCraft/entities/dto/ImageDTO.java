package ru.rsreu.MosaiCraft.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rsreu.MosaiCraft.entities.database.Image;

@Data
@AllArgsConstructor
public class ImageDTO {
    private String imagePath;

    private Double red;

    private Double green;

    private Double blue;

    public ImageDTO(Image image) {
        this.imagePath = image.getImagePath();
        this.red = image.getRed();
        this.green = image.getGreen();
        this.blue = image.getBlue();
    }
}

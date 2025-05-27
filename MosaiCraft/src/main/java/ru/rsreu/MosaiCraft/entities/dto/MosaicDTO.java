package ru.rsreu.MosaiCraft.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import ru.rsreu.MosaiCraft.entities.database.Mosaic;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Data
@AllArgsConstructor
public class MosaicDTO {

    private long id;

    private String name;

    private String imageBase64;

    public MosaicDTO(Mosaic mosaic) throws IOException {
        this.id = mosaic.getId();
        this.name = mosaic.getName();
        this.imageBase64 = convertImageToBase64(mosaic.getMosaicPath());
    }

    private String convertImageToBase64(String imagePath) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(imagePath));
        return Base64.getEncoder().encodeToString(fileContent);
    }
}

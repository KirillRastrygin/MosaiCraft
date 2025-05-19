package ru.rsreu.MosaiCraft.entities.create_mosaic;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;
import ru.rsreu.MosaiCraft.entities.database.Template;

@Data
@Getter
@Setter
public class MosaicData {

    @NotNull
    private MultipartFile inputImage;

    private Template template;

    @NotBlank
    private String name;

    @Min(value = 1, message = "Минимальный размер плитки — 1")
    @Max(value = 120, message = "Максимальный размер плитки — 120")
    private int tileSize;

    @Min(value = 0, message = "Минимальное расстояние — 0")
    @Max(value = 10, message = "Максимальное расстояние — 10")
    private int minDistance;

    private int mosaicSize;
    private String outputImage;

    public MosaicData() {
    }

    public MosaicData(String outputImage) {
        this.outputImage = outputImage;
    }


}

package ru.rsreu.MosaiCraft.entities.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rsreu.MosaiCraft.entities.database.Mosaic;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDTO {
    @NotBlank(message = "Название обязательно")
    @Size(min = 1, max = 50, message = "Название должно быть от 1 до 50 символов")
    private String name;

    private List<MosaicDTO> mosaics;

    @Size(min = 1)
    private List<Long> selectedMosaics;
}

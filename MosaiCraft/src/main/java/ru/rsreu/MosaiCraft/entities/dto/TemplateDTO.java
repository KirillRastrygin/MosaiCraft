package ru.rsreu.MosaiCraft.entities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDTO {
    @NotBlank(message = "Название обязательно")
    @Size(min = 3, max = 50, message = "Название должно быть от 3 до 50 символов")
    private String name;

    @NotNull(message = "Необходимо загрузить изображения")
    private List<MultipartFile> images;
}

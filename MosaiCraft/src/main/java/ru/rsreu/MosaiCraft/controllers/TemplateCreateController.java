package ru.rsreu.MosaiCraft.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import ru.rsreu.MosaiCraft.entities.database.Image;
import ru.rsreu.MosaiCraft.entities.database.Mosaic;
import ru.rsreu.MosaiCraft.entities.database.Template;
import ru.rsreu.MosaiCraft.entities.database.User;
import ru.rsreu.MosaiCraft.entities.dto.TemplateDTO;
import ru.rsreu.MosaiCraft.services.TemplateService;
import ru.rsreu.MosaiCraft.services.UserService;
import ru.rsreu.MosaiCraft.storage.TemplateStorageManager;
import ru.rsreu.MosaiCraft.utils.Initializer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/template")
@RequiredArgsConstructor
public class TemplateCreateController {


    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    @GetMapping("/create")
    public String showCreateForm(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("templateDTO", new TemplateDTO());
        return "templateCreate";
    }

    @PostMapping("/create")
    public String createTemplate(@Valid @ModelAttribute("templateDTO") TemplateDTO templateDTO,
                                 BindingResult bindingResult, @AuthenticationPrincipal User user) {

        // Дополнительная проверка файлов
        if (templateDTO.getImages() == null || templateDTO.getImages().isEmpty()) {
            bindingResult.rejectValue("images", "NotEmpty", "Необходимо загрузить хотя бы одно изображение");
        } else {
            for (MultipartFile file : templateDTO.getImages()) {
                if (!file.getContentType().equals("image/jpeg")) {
                    bindingResult.rejectValue("images", "invalid.type", "Поддерживаются только JPG изображения");
                    break;
                }
            }
        }

        if (bindingResult.hasErrors()) {
            return "templateCreate";
        }

        InputStream inputStream = null;
        List<BufferedImage> inputImages = new ArrayList<>();
        try {
            for (MultipartFile file : templateDTO.getImages()) {
                inputStream = file.getInputStream();
                // Читаем изображение с помощью ImageIO
                inputImages.add(ImageIO.read(inputStream));
                inputStream.close();
            }

        } catch (IOException e) {
            log.warn("Ошибка загрузки изображения!");
        }

        // Проверяем и создаем директорию, если ее нет
        File storageDir = new File(Initializer.usersTemplatesStoragePath + user.getUsername() + "//" +
                templateDTO.getName() + "//");
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                log.error("Не удалось создать директорию: {}", storageDir.getAbsolutePath());
                throw new RuntimeException("Failed to create storage directory");
            }
        }

        Template template = new Template(templateDTO.getName(), false);

        userService.addUserTemplate(user.getId(), template);

        for (BufferedImage img : inputImages) {
            try {
                String filename = UUID.randomUUID().toString() + ".jpg";
                File file = new File(storageDir.getAbsolutePath()+ "//" + filename);

                if (!ImageIO.write(img, "jpg", file)) {
                    log.error("Не удалось сохранить изображение: формат не поддерживается");
                    throw new RuntimeException("Unsupported image format");
                }
                log.info("Изображение успешно сохранено: {}", file.getAbsolutePath());
            } catch (IOException e) {
                log.error("Ошибка при сохранении изображения", e);
                throw new RuntimeException("Failed to save image", e);
            }

        }
        TemplateStorageManager.addImagesToTemplate(storageDir.getAbsolutePath(), template);

        userService.updateUser(user);

        return "redirect:/profile";
    }
}
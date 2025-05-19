package ru.rsreu.MosaiCraft.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.rsreu.MosaiCraft.entities.create_mosaic.MosaicCreator;
import ru.rsreu.MosaiCraft.entities.create_mosaic.MosaicData;
import ru.rsreu.MosaiCraft.entities.database.Image;
import ru.rsreu.MosaiCraft.entities.database.Mosaic;
import ru.rsreu.MosaiCraft.entities.database.Template;
import ru.rsreu.MosaiCraft.entities.database.User;
import ru.rsreu.MosaiCraft.entities.dto.ImageDTO;
import ru.rsreu.MosaiCraft.services.TemplateService;
import ru.rsreu.MosaiCraft.services.UserService;
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
public class MosaicController {

    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    @GetMapping("/mosaic/create")
    public String mosaic(Model model, @AuthenticationPrincipal User user) {
        List<Template> availableTemplates = templateService.commonTemplates();
        availableTemplates.addAll(templateService.userTemplates(user.getId()));
        MosaicData mosaicData = new MosaicData();
        model.addAttribute("availableTemplates", availableTemplates);
        model.addAttribute("mosaicForm", mosaicData);
        model.addAttribute("mosaicSizes", Initializer.getMosaicSizes());
        return "mosaic";
    }

    @PostMapping("/mosaic/create")
    public String createMosaic(@ModelAttribute("mosaicForm") @Valid MosaicData mosaicForm,
                               Errors errors, Model model,
                               @AuthenticationPrincipal User user) {

        if (errors.hasErrors()) {
            List<Template> availableTemplates = templateService.commonTemplates();
            availableTemplates.addAll(templateService.userTemplates(user.getId()));
            model.addAttribute("availableTemplates", availableTemplates);
            model.addAttribute("mosaicSizes", Initializer.getMosaicSizes());
            return "mosaic";
        }

        InputStream inputStream = null;
        BufferedImage inputImage = null;
        try {
            inputStream = mosaicForm.getInputImage().getInputStream();
            // Читаем изображение с помощью ImageIO
            inputImage = ImageIO.read(inputStream);
            inputStream.close();
        } catch (IOException e) {
            log.warn("Ошибка загрузки изображения!");
        }

        MosaicCreator mosaicCreator = new MosaicCreator();
        BufferedImage mosaicImage = null;
        try {
            List<ImageDTO> imageDTOS = new ArrayList<>();
            for(Image img: mosaicForm.getTemplate().getImages()) {
                imageDTOS.add(new ImageDTO(img));
            }
            mosaicImage = mosaicCreator.mosaic(inputImage, mosaicForm.getTileSize(), mosaicForm.getMinDistance(), mosaicForm.getMosaicSize(),
                    imageDTOS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        // Проверяем и создаем директорию, если ее нет
        File storageDir = new File(Initializer.mosaicStoragePath);
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                log.error("Не удалось создать директорию: {}", Initializer.mosaicStoragePath);
                throw new RuntimeException("Failed to create storage directory");
            }
        }

        String filename = UUID.randomUUID().toString() + ".png";
        File file = new File(Initializer.mosaicStoragePath + filename);

        try {
            if (!ImageIO.write(mosaicImage, "png", file)) {
                log.error("Не удалось сохранить изображение: формат не поддерживается");
                throw new RuntimeException("Unsupported image format");
            }
            log.info("Изображение успешно сохранено: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Ошибка при сохранении изображения", e);
            throw new RuntimeException("Failed to save image", e);
        }

        Mosaic mosaic = new Mosaic(mosaicForm.getName(), Initializer.mosaicStoragePath + filename);
        userService.addUserMosaic(user.getId(), mosaic);
        userService.updateUser(user);

        //TODO: добавление альбома

        return "redirect:/mosaic/create";
    }



}

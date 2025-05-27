package ru.rsreu.MosaiCraft.controllers;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.MosaiCraft.entities.database.*;
import ru.rsreu.MosaiCraft.entities.dto.MosaicDTO;
import ru.rsreu.MosaiCraft.services.TemplateService;
import ru.rsreu.MosaiCraft.services.UserService;

import org.springframework.http.ResponseEntity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    @GetMapping
    public String getProfilePage(Model model, @AuthenticationPrincipal User user) throws IOException {
        User fullUser = userService.findByIdWithMosaicsAndAlbums(user.getId());

        List<Mosaic> mosaics = fullUser.getMosaics();
        List<MosaicDTO> dtoMosaics = new ArrayList<>();
        for (Mosaic mosaic : mosaics) {
            dtoMosaics.add(new MosaicDTO(mosaic));
        }

        model.addAttribute("user", fullUser);
        model.addAttribute("mosaics", dtoMosaics);
        model.addAttribute("albums", fullUser.getAlbums());
        model.addAttribute("templates", templateService.userTemplates(fullUser.getId()));

        return "profile";
    }

    @GetMapping("/mosaic/view/{id}")
    public ResponseEntity<byte[]> viewMosaic(@PathVariable long id) throws IOException {
        Mosaic mosaic = userService.findMosaicById(id); // Получаем мозаику по ID
        Path imagePath = Paths.get(mosaic.getMosaicPath()); // Путь к изображению

        // Кешируем изображение на 1 день
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .contentType(MediaType.IMAGE_JPEG)
                .body(Files.readAllBytes(imagePath));
    }

    @GetMapping("/mosaic/download/{id}")
    public ResponseEntity<byte[]> downloadMosaic(@PathVariable long id) throws IOException {
        Mosaic mosaic = userService.findMosaicById(id);
        Path imagePath = Paths.get(mosaic.getMosaicPath());

        byte[] imageBytes = Files.readAllBytes(imagePath);
        String contentType = Files.probeContentType(imagePath);
        String filename = mosaic.getName() + getFileExtension(mosaic.getMosaicPath()); // Добавляем расширение

        // Устанавливаем заголовок Content-Disposition для принудительного скачивания
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(imageBytes);
    }

    // Получаем расширение файла (например, ".jpg")
    private String getFileExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf("."));
    }

    @PostMapping("/mosaic/delete/{id}")
    public String deleteMosaic(@PathVariable long id, @AuthenticationPrincipal User user) throws IOException {
        Mosaic mosaic = userService.findMosaicById(id);

        // Удаляем файл
        Path imagePath = Paths.get(mosaic.getMosaicPath());
        Files.deleteIfExists(imagePath);

        // Удаляем запись из БД
        userService.deleteMosaic(user.getId(), id);

        return "redirect:/profile"; // Перенаправляем обратно в профиль
    }

    @GetMapping("/template/view/{id}")
    public String viewTemplatePage(@PathVariable Long id, Model model) {
        Template template = templateService.findTemplateById(id);
        model.addAttribute("template", template);

        // Конвертируем изображения в Base64
        List<String> base64Images = template.getImages().stream()
                .map(image -> {
                    try {
                        byte[] bytes = Files.readAllBytes(Paths.get(image.getImagePath()));
                        return Base64.getEncoder().encodeToString(bytes);
                    } catch (IOException e) {
                        throw new RuntimeException("Ошибка чтения изображения", e);
                    }
                })
                .collect(Collectors.toList());

        model.addAttribute("images", base64Images);
        return "template-view"; // Шаблон Thymeleaf
    }

    @GetMapping("/template/download/{id}")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable Long id) throws IOException {
        Template template = templateService.findTemplateById(id);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Image image : template.getImages()) {
                Path imagePath = Paths.get(image.getImagePath());
                String fileName = imagePath.getFileName().toString();

                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                Files.copy(imagePath, zos);
                zos.closeEntry();
            }
        }

        // Транслитерируем русские буквы
        String transliterated = toTranslit(template.getName());
        String safeFileName = transliterated
                .replaceAll("[^a-zA-Z0-9.-]", "_")
                .trim();

        String zipFileName = safeFileName + ".zip";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + zipFileName + "\"")
                .body(new ByteArrayResource(baos.toByteArray()));
    }

    // Метод для транслитерации
    private String toTranslit(String text) {
        HashMap<Character, String> map = new HashMap<>();
        map.put('а', "a"); map.put('б', "b"); map.put('в', "v"); map.put('г', "g");
        map.put('д', "d"); map.put('е', "e"); map.put('ё', "yo"); map.put('ж', "zh");
        map.put('з', "z"); map.put('и', "i"); map.put('й', "y"); map.put('к', "k");
        map.put('л', "l"); map.put('м', "m"); map.put('н', "n"); map.put('о', "o");
        map.put('п', "p"); map.put('р', "r"); map.put('с', "s"); map.put('т', "t");
        map.put('у', "u"); map.put('ф', "f"); map.put('х', "h"); map.put('ц', "ts");
        map.put('ч', "ch"); map.put('ш', "sh"); map.put('щ', "sch"); map.put('ъ', "");
        map.put('ы', "y"); map.put('ь', ""); map.put('э', "e"); map.put('ю', "yu");
        map.put('я', "ya");

        StringBuilder result = new StringBuilder();
        for (char c : text.toLowerCase().toCharArray()) {
            result.append(map.getOrDefault(c, String.valueOf(c)));
        }
        return result.toString();
    }

    @PostMapping("/template/delete/{id}")
    public String deleteTemplate(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) throws IOException {
        Template template = templateService.findTemplateById(id);

        // Проверяем, что шаблон принадлежит пользователю
        if (!template.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Нельзя удалить чужой шаблон");
        }

        // Удаляем файлы изображений
        for (Image image : template.getImages()) {
            Path imagePath = Paths.get(image.getImagePath());
            Files.deleteIfExists(imagePath);
        }

        // Удаляем шаблон из БД
        templateService.deleteTemplate(id);

        return "redirect:/profile"; // Перенаправляем обратно в профиль
    }

    @GetMapping("/album/view/{id}")
    public String viewAlbumPage(@PathVariable Long id, Model model) {
        Album album = userService.findAlbumById(id);
        model.addAttribute("album", album);

        // Преобразуем изображения мозаик в Base64
        List<String> base64Images = album.getMosaics().stream()
                .map(mosaic -> {
                    try {
                        byte[] bytes = Files.readAllBytes(Paths.get(mosaic.getMosaicPath()));
                        return Base64.getEncoder().encodeToString(bytes);
                    } catch (IOException e) {
                        throw new RuntimeException("Ошибка чтения изображения", e);
                    }
                })
                .collect(Collectors.toList());

        model.addAttribute("images", base64Images);
        return "album-view"; // шаблон Thymeleaf для просмотра альбома
    }

    @GetMapping("/album/download/{id}")
    public ResponseEntity<Resource> downloadAlbum(@PathVariable Long id) throws IOException {
        Album album = userService.findAlbumById(id);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Mosaic mosaic : album.getMosaics()) {
                Path imagePath = Paths.get(mosaic.getMosaicPath());
                String fileName = imagePath.getFileName().toString();

                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                Files.copy(imagePath, zos);
                zos.closeEntry();
            }
        }

        String transliterated = toTranslit(album.getName());
        String safeFileName = transliterated.replaceAll("[^a-zA-Z0-9.-]", "_").trim();
        String zipFileName = safeFileName + ".zip";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + zipFileName + "\"")
                .body(new ByteArrayResource(baos.toByteArray()));
    }

    @PostMapping("/album/delete/{id}")
    public String deleteAlbum(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) throws IOException {
        Album album = userService.findAlbumById(id);

        // Проверка прав доступа
        if (!album.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Нельзя удалить чужой альбом");
        }

        // Физически удалять изображения не требуется, так как мозаики могут использоваться в других альбомах
        userService.deleteAlbumById(id);

        return "redirect:/profile"; // назад в профиль
    }


}

package ru.rsreu.MosaiCraft.controllers;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.MosaiCraft.entities.database.Album;
import ru.rsreu.MosaiCraft.entities.database.Mosaic;
import ru.rsreu.MosaiCraft.entities.database.Template;
import ru.rsreu.MosaiCraft.entities.database.User;
import ru.rsreu.MosaiCraft.services.TemplateService;
import ru.rsreu.MosaiCraft.services.UserService;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    @GetMapping
    public String getProfilePage(Model model, @AuthenticationPrincipal User user) {
        User fullUser = userService.findByIdWithMosaicsAndAlbums(user.getId());

        model.addAttribute("user", fullUser);
        model.addAttribute("mosaics", fullUser.getMosaics());
        model.addAttribute("albums", fullUser.getAlbums());
        model.addAttribute("templates", templateService.userTemplates(fullUser.getId()));

        return "profile";
    }

    @PostMapping("/mosaic/delete/{id}")
    public String deleteMosaic(@PathVariable Long id, @AuthenticationPrincipal User user) {
        userService.deleteMosaicById(user.getId(), id);
        return "redirect:/profile";
    }

    @PostMapping("/template/delete/{id}")
    public String deleteTemplate(@PathVariable Long id, @AuthenticationPrincipal User user) {
        templateService.deleteTemplateById(id);
        return "redirect:/profile";
    }

    @PostMapping("/album/delete/{id}")
    public String deleteAlbum(@PathVariable Long id, @AuthenticationPrincipal User user) {
        userService.deleteAlbumById(user.getId(), id);
        return "redirect:/profile";
    }

//    @GetMapping("/template/download/{id}")
//    public ResponseEntity<Resource> downloadTemplate(@PathVariable Long id, @AuthenticationPrincipal User user) throws IOException {
//        Resource archive = templateService.downloadTemplateArchive(id);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=template.zip")
//                .body(archive);
//    }

//    @GetMapping("/mosaic/download/{id}")
//    public ResponseEntity<Resource> downloadMosaic(@PathVariable Long id, @AuthenticationPrincipal User user) {
//        Resource mosaic = userService.downloadMosaic(user.getId(), id);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mosaic.png")
//                .body(mosaic);
//    }
//
//    @GetMapping("/album/download/{id}")
//    public ResponseEntity<Resource> downloadAlbum(@PathVariable Long id, @AuthenticationPrincipal User user) {
//        Resource archive = userService.downloadAlbumArchive(user.getId(), id);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=album.zip")
//                .body(archive);
//    }

    @PostMapping("/album/create")
    public String createAlbum(@RequestParam String name, @AuthenticationPrincipal User user) {
        userService.createAlbum(user.getId(), new Album(name));
        return "redirect:/profile";
    }
}

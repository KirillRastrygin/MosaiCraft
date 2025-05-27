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
import ru.rsreu.MosaiCraft.entities.database.Album;
import ru.rsreu.MosaiCraft.entities.database.Mosaic;
import ru.rsreu.MosaiCraft.entities.database.User;

import ru.rsreu.MosaiCraft.entities.dto.AlbumDTO;
import ru.rsreu.MosaiCraft.entities.dto.MosaicDTO;
import ru.rsreu.MosaiCraft.entities.dto.TemplateDTO;
import ru.rsreu.MosaiCraft.services.TemplateService;
import ru.rsreu.MosaiCraft.services.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/album")
@RequiredArgsConstructor
public class AlbumCreateController {
    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    @GetMapping("/create")
    public String showCreateForm(Model model, @AuthenticationPrincipal User user) throws IOException {
        User fullUser = userService.findByIdWithMosaicsAndAlbums(user.getId());
        List<Mosaic> mosaics = fullUser.getMosaics();
        List<MosaicDTO> dtoMosaics = new ArrayList<>();
        for (Mosaic mosaic : mosaics) {
            dtoMosaics.add(new MosaicDTO(mosaic));
        }
        AlbumDTO albumDTO = new AlbumDTO();
        albumDTO.setMosaics(dtoMosaics);
        model.addAttribute("albumDTO", albumDTO);
        return "albumCreate";
    }

    @PostMapping("/create")
    public String createTemplate(@Valid @ModelAttribute("albumDTO") AlbumDTO albumDTO,
                                 BindingResult bindingResult, @AuthenticationPrincipal User user) {
        if (bindingResult.hasErrors()) {
            return "albumCreate";
        }
        Album album = new Album(albumDTO.getName());

        if (albumDTO.getSelectedMosaics() != null) {
            for (Long mosaicID : albumDTO.getSelectedMosaics()) {
                album.addMosaic(userService.findMosaicById(mosaicID));
            }
        }

            userService.addUserAlbum(user.getId(), album);
            userService.updateUser(user);

        return "redirect:/profile";
    }
}


package ru.rsreu.MosaiCraft.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.MosaiCraft.entities.database.Image;
import ru.rsreu.MosaiCraft.entities.database.Template;
import ru.rsreu.MosaiCraft.entities.database.User;
import ru.rsreu.MosaiCraft.repo.TemplateRepository;
import ru.rsreu.MosaiCraft.repo.UserRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class TemplateService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private UserRepository userRepository;

    public Template findTemplateById(Long templateId) {
        Optional<Template> templateFromDb = templateRepository.findById(templateId);
        return templateFromDb.orElse(new Template());
    }

    public List<Template> allTemplates() {
        return templateRepository.findAll();
    }

    public List<Template> userTemplates(Long userId) {
        return templateRepository.findByUserId(userId);
    }

    public List<Template> commonTemplates() {
        return templateRepository.findByIsCommonTrue();
    }

    public boolean saveTemplate(Template template, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        template.setUser(user);
        templateRepository.save(template);
        return true;
    }

    public boolean saveCommonTemplate(Template template) {
        if (!template.isCommon() && !(template.getUser() == null)) {
            return false;
        }
        templateRepository.save(template);
        return true;
    }

    public boolean updateTemplate(Template template) {
        if (!templateRepository.existsById(template.getId())) {
            return false;
        }

        templateRepository.save(template);
        return true;
    }

    public boolean deleteTemplate(Long templateId) {
        if (templateRepository.existsById(templateId)) {
            templateRepository.deleteById(templateId);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean addImageToTemplate(Long templateId, Image image) {
        Template template = templateRepository.findById(templateId).orElse(null);
        if (template == null) {
            return false;
        }

        template.addImage(image);
        templateRepository.save(template);
        return true;
    }

    // Методы для скачивания шаблонов и удаления их
    public boolean downloadTemplate(Long templateId) {
        Template template = templateRepository.findById(templateId).orElse(null);
        if (template == null) {
            return false;
        }

        // Логика для скачивания шаблона
        // Например, можно использовать поток данных для создания архива или скачивания файлов
        return true;
    }

    public boolean deleteTemplateById(Long templateId) {
        Optional<Template> template = templateRepository.findById(templateId);
        if (template.isPresent()) {
            templateRepository.delete(template.get());
            return true;
        }
        return false;
    }
    public byte[] downloadTemplateArchive(Long templateId) throws IOException {
        Template template = templateRepository.findById(templateId).orElseThrow();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);

        for (Image image : template.getImages()) {
            ZipEntry entry = new ZipEntry(image.getImagePath());
            zipOut.putNextEntry(entry);
            zipOut.closeEntry();
        }

        zipOut.close();
        return baos.toByteArray();
    }
}

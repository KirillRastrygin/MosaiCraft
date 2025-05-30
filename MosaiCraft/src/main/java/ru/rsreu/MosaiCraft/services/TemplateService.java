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

    public List<Template> userTemplates(Long userId) {
        return templateRepository.findByUserId(userId);
    }

    public List<Template> commonTemplates() {
        return templateRepository.findByIsCommonTrue();
    }


    public boolean saveCommonTemplate(Template template) {
        if (!template.isCommon() && !(template.getUser() == null)) {
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


}

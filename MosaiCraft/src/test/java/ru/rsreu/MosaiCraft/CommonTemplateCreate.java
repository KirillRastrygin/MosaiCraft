package ru.rsreu.MosaiCraft;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.rsreu.MosaiCraft.services.TemplateService;
import ru.rsreu.MosaiCraft.services.UserService;
import ru.rsreu.MosaiCraft.storage.TemplateStorageManager;

@SpringBootTest
public class CommonTemplateCreate {
    @Autowired
    private UserService userService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    TemplateStorageManager templateStorageManager;

    @Test
    public void createCommonTemplate() {
        templateStorageManager.readCommonTemplate("картины");

    }

}

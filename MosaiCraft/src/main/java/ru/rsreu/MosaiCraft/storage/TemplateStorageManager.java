package ru.rsreu.MosaiCraft.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rsreu.MosaiCraft.entities.database.Image;
import ru.rsreu.MosaiCraft.entities.database.Template;
import ru.rsreu.MosaiCraft.services.TemplateService;
import ru.rsreu.MosaiCraft.utils.Initializer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class TemplateStorageManager {

    private static TemplateService templateService;

    @Autowired
    public void setTemplateService(TemplateService templateService) {
        TemplateStorageManager.templateService = templateService;
    }

    public static void storeTemplate(Template template) {

    }

    // Метод для добавления общего шаблона
    public static void readCommonTemplate(String templateName) {

        String inputPath = Initializer.commonTemplateStoragePath + templateName;

        //Проверка наличия папки
        File file = new File(inputPath);
        if (!file.exists()){
            log.error("Нет такой папки!{}", inputPath);
        }


        Template template = new Template();
        template.setName(templateName);
        template.setCommon(true);

        addImagesToTemplate(inputPath, template);



    }

    public static void addImagesToTemplate(String folderPath, Template template) {

        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file.getName())) {
                    try {
                        BufferedImage currentImage = ImageIO.read(file);
                        if (currentImage != null) {
                            double[] avgRGB = calculateAverageRGB(currentImage);
                            Image image = new Image(template,
                                    file.getAbsolutePath(),
                                    avgRGB[0], avgRGB[1], avgRGB[2]);
                            template.addImage(image);
                        }
                    } catch (IOException e) {
                        System.err.println("Error processing image: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }

        templateService.saveCommonTemplate(template);

    }

    private static double[] calculateAverageRGB(BufferedImage image) {
        long totalRed = 0, totalGreen = 0, totalBlue = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        int pixelCount = width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                totalRed += (pixel >> 16) & 0xff;
                totalGreen += (pixel >> 8) & 0xff;
                totalBlue += pixel & 0xff;
            }
        }

        return new double[]{
                (double) totalRed / pixelCount,
                (double) totalGreen / pixelCount,
                (double) totalBlue / pixelCount
        };
    }
    private static boolean isImageFile(String fileName) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".bmp"};
        String lowerCaseFileName = fileName.toLowerCase();

        for (String extension : imageExtensions) {
            if (lowerCaseFileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

}

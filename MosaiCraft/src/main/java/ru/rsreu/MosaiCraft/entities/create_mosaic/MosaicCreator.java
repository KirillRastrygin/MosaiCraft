package ru.rsreu.MosaiCraft.entities.create_mosaic;

import org.springframework.beans.factory.annotation.Autowired;
import ru.rsreu.MosaiCraft.entities.database.Image;
import ru.rsreu.MosaiCraft.entities.database.Template;
import ru.rsreu.MosaiCraft.entities.dto.ImageDTO;
import ru.rsreu.MosaiCraft.services.TemplateService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MosaicCreator {



    public BufferedImage mosaic(BufferedImage img, int tileSize, int minDistance,
                                       int mosaicSize, List<ImageDTO> templateImages) throws InterruptedException, IOException {
        long t0 = System.currentTimeMillis();

        // Получаем соотношение мозайки к оригинальному изображению
        int mosaicSizeRatio = getMosaicSizeRatio(img, mosaicSize);

        // Получаем размеры оригинального изображения
        int width = img.getWidth();
        int height = img.getHeight();


        // Разделяем оригинальное изображение на четыре части
        BlockingQueue<BufferedImage> c1 = cutWithChannel(img, templateImages, tileSize, mosaicSizeRatio, minDistance,
                0, 0, width / 2, height / 2);
        BlockingQueue<BufferedImage> c2 = cutWithChannel(img, templateImages, tileSize, mosaicSizeRatio, minDistance,
                width / 2, 0, width, height / 2);
        BlockingQueue<BufferedImage> c3 = cutWithChannel(img, templateImages, tileSize, mosaicSizeRatio, minDistance,
                0, height / 2, width / 2, height);
        BlockingQueue<BufferedImage> c4 = cutWithChannel(img, templateImages, tileSize, mosaicSizeRatio, minDistance,
                width / 2, height / 2, width, height);

        Rectangle combineRect = new Rectangle(0, 0, (width * mosaicSizeRatio),
                (height * mosaicSizeRatio));
        // Соединяем полученные изображения
        BufferedImage result = combine(combineRect, c1, c2, c3, c4).take();

        long t1 = System.currentTimeMillis();

        // Выводим результаты обработки
        System.out.println("Продолжительность: " + (t1 - t0) + " ms");

        // Возвращаем результаты обработки
        return result;

    }

    // ищет максимально близко совпадающее изображение
    public static String nearest(double[] target, List<ImageDTO> images, Queue<String> usedImagesQueue) {
        String fileName = null;
        double smallest = 1000000.0;
        boolean identical;

        for (ImageDTO image : images) {
            double dist = distance(target, image);
            identical = false;

            if (dist < smallest) {
                //Проверка на совпадение картинки с картинкой в очереди
                for (String element : usedImagesQueue) {
                    if (Objects.equals(image.getImagePath(), element)) {
                        identical = true;
                        break;
                    }
                }

                // Если картинки разные то объявляем картинку ближайшей
                if (!identical) {
                    fileName = image.getImagePath();
                    smallest = dist;
                } else {
                    continue;
                }
            }

        }

        return fileName;
    }

    // возвращает Евклидово расстояние между двумя точками
    public static double distance(double[] p1, ImageDTO image) {
        return Math.sqrt(sq(image.getRed() - p1[0]) + sq(image.getGreen() - p1[1]) + sq(image.getBlue() - p1[2]));
    }

    // возвращает квадрат
    public static double sq(double n) {
        return n * n;
    }

    // изменяем размер изображения на новое значение newWidth
    public static BufferedImage resize(BufferedImage inputImg, int newWidth) {
        int width = inputImg.getWidth();
        int height = inputImg.getHeight();
        double ratio = (double) width / newWidth;
        double stepX = 0.0, stepY = 0.0;

        BufferedImage outputImg = new BufferedImage((int) (newWidth), (int) (height / ratio), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {
                int rgb = inputImg.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb) & 0xFF;

                stepX = (x / ratio);
                if (stepX > (int) (newWidth))
                    stepX--;

                stepY = (y / ratio);
                if (stepY > (int) (height / ratio))
                    stepY--;

                outputImg.setRGB((int) stepX, (int) stepY,
                        (alpha << 24) | (red << 16) | (green << 8) | blue);

            }

        }

        return outputImg;
    }

    // Функция обрезки изображения с использованием блокирующей очереди
    public static BlockingQueue<BufferedImage> cutWithChannel(BufferedImage original, List<ImageDTO> images,
                                                              int tileSize, int mosaicSizeRatio, int minDistanceBetweenIdenticalImages,
                                                              int x1, int y1, int x2, int y2) {
        BlockingQueue<BufferedImage> queue = new LinkedBlockingQueue<>();
        // Создаем новый поток для обработки изображения.
        new Thread(() -> {
            // Создаем новое изображение, которое будет содержать результат обработки.
            BufferedImage newImage = new BufferedImage(((x2 - x1) * mosaicSizeRatio),
                    ((y2 - y1) * mosaicSizeRatio), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = newImage.createGraphics();

            // Создаем массив для хранения адресов картинок
            String[][] tileMatrix = new String[(y2-y1)][(x2-x1)];

            // Создание очереди с использованными картинками для минимального
            // расстояния между одинаковыми картинками
            Queue<String> usedImagesQueue = new LinkedList<>();

            // Проходим по всем координатам изображения с шагом равным размеру плитки.
            for (int y = 0, tileY = 0, i = 0; y < y2; y += (tileSize / mosaicSizeRatio), tileY += tileSize, i++) {
                for (int x = 0, tileX = 0, j = 0; x < x2; x += (tileSize / mosaicSizeRatio), tileX += tileSize, j++) {
                    // Получаем цвет пикселя из оригинального изображения.
                    int rgba = original.getRGB(x, y);

                    // Извлекаем красный (R), зеленый (G) и синий (B) каналы
                    int red = (rgba >> 16) & 0xFF; // Красный канал
                    int green = (rgba >> 8) & 0xFF; // Зеленый канал
                    int blue = rgba & 0xFF; // Синий канал

                    double[] color = {red, green, blue};
                    //Добавляем в очередь использованные поблизости картинки
                    for (int k = i + minDistanceBetweenIdenticalImages; k >= i-minDistanceBetweenIdenticalImages; k--){
                        for (int l = j + minDistanceBetweenIdenticalImages; l >= j-minDistanceBetweenIdenticalImages; l--){
                            if (k < 0 || l < 0) {
                                continue;
                            }
                            usedImagesQueue.offer(tileMatrix[k][l]);
                        }
                    }

                    // Находим ближайшую плитку в базе данных.
                    String nearestTile = nearest(color, images, usedImagesQueue);
                    // Добавляем адрес подходящей плитки в массив с адресами плиток
                    tileMatrix[i][j] = nearestTile;
                    //Очищаем оцередь с адресами плиток поблизости
                    usedImagesQueue.clear();
                    try {
                        // Читаем изображение выбранной плитки из файла.

                        BufferedImage img = ImageIO.read(new File(nearestTile));
                        // Изменяем размер плитки на заданный tileSize.
                        BufferedImage resizedTile = resize(img, tileSize);
                        // Рисуем измененную плитку на новом изображении.
                        g2d.drawImage(resizedTile, (tileX - x1 * mosaicSizeRatio),
                                (tileY - y1 * mosaicSizeRatio), null);

                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }

            // Завершаем работу графического контекста.
            g2d.dispose();

            // Добавляем обработанное изображение в блокирующую очередь.
            queue.offer(newImage);
        }).start();
        // Возвращаем блокирующую очередь с обработанным изображением.
        return queue;
    }

    // Функция для комбинирования изображений из четырех каналов в одно изображение
    public static BlockingQueue<BufferedImage> combine(Rectangle rectangle, BlockingQueue<BufferedImage> c1,
                                                       BlockingQueue<BufferedImage> c2, BlockingQueue<BufferedImage> c3,
                                                       BlockingQueue<BufferedImage> c4) {
        BlockingQueue<BufferedImage> resultBlockingQueue = new LinkedBlockingQueue<>(); // Создаем блокирующую очередь для результата
        new Thread(() -> { // Создаем новый поток для обработки изображений
            try {
                // Создаем новое изображение
                BufferedImage img = new BufferedImage(rectangle.width, rectangle.height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = img.createGraphics(); // Получаем графический контекст для рисования на изображении
                for (int i = 0; i < 4; i++) { // Цикл по четырем каналам
                    BufferedImage CurrentImg = null; // Переменная для текущего изображения из канала
                    BlockingQueue<BufferedImage> channel = null; // Переменная для текущего канала
                    int x = 0, y = 0; // Координаты для рисования изображения на общем изображении
                    switch (i) { // Определение текущего канала и его координат на общем изображении
                        case 0:
                            CurrentImg = c1.take(); // Получаем изображение из первого канала
                            channel = c1; // Устанавливаем текущий канал
                            x = rectangle.x; // Устанавливаем координату X
                            y = rectangle.y; // Устанавливаем координату Y
                            break;
                        case 1:
                            CurrentImg = c2.take(); // Получаем изображение из второго канала
                            channel = c2; // Устанавливаем текущий канал
                            x = rectangle.x + rectangle.width / 2; // Устанавливаем координату X
                            y = rectangle.y; // Устанавливаем координату Y
                            break;
                        case 2:
                            CurrentImg = c3.take(); // Получаем изображение из третьего канала
                            channel = c3; // Устанавливаем текущий канал
                            x = rectangle.x; // Устанавливаем координату X
                            y = rectangle.y + rectangle.height / 2; // Устанавливаем координату Y
                            break;
                        case 3:
                            CurrentImg = c4.take(); // Получаем изображение из четвертого канала
                            channel = c4; // Устанавливаем текущий канал
                            x = rectangle.x + rectangle.width / 2; // Устанавливаем координату X
                            y = rectangle.y + rectangle.height / 2; // Устанавливаем координату Y
                            break;
                    }
                    if (CurrentImg != null) { // Если изображение получено из канала
                        g2d.drawImage(CurrentImg, x, y, null); // Наносим изображение на общее изображение
                        channel.put(CurrentImg); // Помещаем изображение обратно в канал
                    }
                }
                g2d.dispose(); // Освобождаем графический контекст

                // Помещаем изображение в результативную очередь
                resultBlockingQueue.offer(img);
            } catch (InterruptedException e) { // Обработка исключений
                e.printStackTrace(); // Вывод стека вызовов исключения
            }
        }).start(); // Запускаем поток для обработки изображений
        return resultBlockingQueue; // Возвращаем результативную очередь
    }

    //найти соотношение изображений
    private static int getMosaicSizeRatio(BufferedImage inputImg, int mosaicSize) {
        if (mosaicSize == 0) {
            return 1;
        } else {
            double ratio = Math.sqrt((double) (mosaicSize / (inputImg.getHeight() * inputImg.getWidth())));
            if (ratio >= 1) {
                return (int) Math.round(ratio);
            } else {
                return 1;
            }

        }
    }
}

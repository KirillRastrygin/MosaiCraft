package ru.rsreu.MosaiCraft.utils;

import ru.rsreu.MosaiCraft.entities.create_mosaic.MosaicSize;

import java.util.ArrayList;
import java.util.List;

public class Initializer {
    public final static String commonTemplateStoragePath = "C:\\Users\\Kirill\\Desktop\\ВКР\\Датасеты\\";
    public final static String mosaicStoragePath = "C:\\Users\\Kirill\\Desktop\\ВКР\\Мозайки\\";
    public final static String usersTemplatesStoragePath = "C:\\Users\\Kirill\\Desktop\\ВКР\\Шаблоны пользователей\\";

    private static List<MosaicSize> mosaicSizes = new ArrayList<>();
    public static List<MosaicSize> getMosaicSizes() {
        mosaicSizes.add(new MosaicSize(0, "Оригинал"));
        mosaicSizes.add(new MosaicSize(2073600, "2 мегапикс. = 1920x1080"));
        mosaicSizes.add(new MosaicSize(6222946, "6 мегапикс. = 3326x1871"));
        mosaicSizes.add(new MosaicSize(12582912, "13 мегапикс. = 4096×3072"));
        mosaicSizes.add(new MosaicSize(24176640, "24 мегапикс. = 7680x3148"));
        mosaicSizes.add(new MosaicSize(61882363, "62 мегапикс. = 12288x5036"));
        return mosaicSizes;
    }


}

package com.project.crawl.service;

import com.project.crawl.util.CommonUtil;
import com.project.crawl.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResizeService {
    @Value("${local.directory}")
    private String localDirectory;

    @Value("${local.daily.directory}")
    private String localDailyDirectory;


    private final ImageUtil imageUtil;
    private final CommonUtil commonUtil;
    public List<String> resizeDailyDirectoryImages(String formatToday) throws IOException {
        String dailyImagesDirectory = String.join("/", localDailyDirectory, formatToday, "images");
        File[] files = new File(dailyImagesDirectory).listFiles();
        List<String> exceptList = new ArrayList<>();

        if (Objects.isNull(files)) return exceptList;

        Set<File> fileSet = Arrays.stream(files)
                .filter(file -> file.isFile() && !file.isHidden() && !file.getName().startsWith("."))
                .collect(Collectors.toSet());

        String resizedDirectory = String.join("/", localDirectory, "/images_resized");
        String resizedSmallDirectory = String.join("/", localDirectory, "/images_resized/small");
        String resizedMediumDirectory = String.join("/", localDirectory, "/images_resized/medium");
        String resizedTinyDirectory = String.join("/", localDirectory, "/images_resized/tiny");
        String dailySmallDirectory = String.join("/", localDailyDirectory, formatToday, "small");
        String dailyMediumDirectory = String.join("/", localDailyDirectory, formatToday, "medium");
        String dailyTinyDirectory = String.join("/", localDailyDirectory, formatToday, "tiny");
        commonUtil.generateDirectories(resizedDirectory,resizedSmallDirectory, resizedMediumDirectory, resizedTinyDirectory, dailySmallDirectory, dailyMediumDirectory, dailyTinyDirectory);

        ImageInputStream in = null;
        BufferedImage img = null;

        for (File file : fileSet) {
            log.debug("resizing file name: {}", file.getName());
            String fileRoute = localDirectory + "/images/" + file.getName();
            try {
                in = ImageIO.createImageInputStream(new File(fileRoute));
                img = ImageIO.read(in);
                if (!commonUtil.fileExists(resizedSmallDirectory, file.getName())) {
                    BufferedImage img300 = imageUtil.resizeImage(img, 300, 300);
                    imageUtil.writeImage(img300, dailySmallDirectory, file.getName());
                    imageUtil.writeImage(img300, resizedSmallDirectory, file.getName());
                    imageUtil.writeImage(img300, dailyTinyDirectory, file.getName());
                    imageUtil.writeImage(img300, resizedTinyDirectory, file.getName());
                }
                if (!commonUtil.fileExists(resizedMediumDirectory, file.getName())) {
                    BufferedImage img500 = imageUtil.resizeImage(img, 500, 500);
                    imageUtil.writeImage(img500, resizedMediumDirectory, file.getName());
                    imageUtil.writeImage(img500, dailyMediumDirectory, file.getName());
                }
            } catch (Exception e) {
                log.debug("error occurred resizing {} : {}", file.getName(), e.getMessage());
                exceptList.add(file.getName());
            }
        }
        commonUtil.close(in); // ImageInputStream 제거
        assert img != null;
        img.flush(); // BufferedImage 제거

        return exceptList;
    }
}

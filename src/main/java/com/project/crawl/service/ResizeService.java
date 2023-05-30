package com.project.crawl.service;

import com.project.crawl.exceptions.CrawlException;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResizeService {
    @Value("${local.directory}")
    private String localDirectory;

    private final ImageUtil imageUtil;
    private final CommonUtil commonUtil;

    public List<String> resizeEntireDirectoryImages(String formatToday) throws IOException {
        String imagesDirectory = String.join("/", localDirectory, "images");
        File[] files = new File(imagesDirectory).listFiles();
        List<String> exceptList = new ArrayList<>();

        if (Objects.isNull(files)) {
            return exceptList;
        }

        String[] resizedDirectories = {
                "images_resized",
                "images_resized/small",
                "images_resized/medium",
                "images_resized/tiny"
        };

        String[] dailyDirectories = {
                String.join("/", "daily", formatToday, "small"),
                String.join("/", "daily", formatToday, "medium"),
                String.join("/", "daily", formatToday, "tiny")
        };

        commonUtil.generateDirectories(Stream.concat(Arrays.stream(resizedDirectories), Arrays.stream(dailyDirectories))
                .map(path -> Path.of(localDirectory, path))
                .toArray(Path[]::new));

        for (File file : files) {
            if (file.isFile() && !file.isHidden() && !file.getName().startsWith(".")) {
                String fileName = file.getName();
                try {
                    BufferedImage originalImage = ImageIO.read(new File(file.getPath()));

                    BufferedImage img300 = imageUtil.resizeImage(originalImage, 300, 300);
                    Stream.of(dailyDirectories[0], resizedDirectories[1], dailyDirectories[2], resizedDirectories[3])
                            .forEach(directory -> imageUtil.writeImage(img300, Path.of(localDirectory, directory, fileName)));

                    BufferedImage img500 = imageUtil.resizeImage(originalImage, 500, 500);
                    Stream.of(resizedDirectories[2], dailyDirectories[1])
                            .forEach(directory -> imageUtil.writeImage(img500, Path.of(localDirectory, directory, fileName)));
                } catch (Exception e) {
                    if (e instanceof FileNotFoundException) {
                        // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                        throw e;
                    }
                    log.debug("Error occurred while resizing {}: {}", fileName, e.getMessage());
                    exceptList.add(fileName);
                }
            }
        }
        return exceptList;
    }

    public List<String> resizeDailyDirectoryImages(String formatToday) throws IOException {
        String dailyImagesDirectory = String.join("/", localDirectory, "daily", formatToday, "images");
        File[] files = new File(dailyImagesDirectory).listFiles();
        List<String> exceptList = new ArrayList<>();

        if (Objects.isNull(files)) {
            return exceptList;
        }

        Set<File> fileSet = Arrays.stream(files)
                .filter(file -> file.isFile() && !file.isHidden() && !file.getName().startsWith("."))
                .collect(Collectors.toSet());

        String[] resizedDirectories = {
                "images_resized",
                "images_resized/small",
                "images_resized/medium",
                "images_resized/tiny"
        };

        String[] dailyDirectories = {
                String.join("/", "daily", formatToday, "small"),
                String.join("/", "daily", formatToday, "medium"),
                String.join("/", "daily", formatToday, "tiny")
        };

        commonUtil.generateDirectories(Stream.concat(Arrays.stream(resizedDirectories), Arrays.stream(dailyDirectories))
                .map(path -> Path.of(localDirectory, path))
                .toArray(Path[]::new));

        for (File file : fileSet) {
            String fileName = file.getName();
            log.debug("resizing file name: {}", fileName);
            try {
                BufferedImage img = ImageIO.read(new File(file.getPath()));

                if (!commonUtil.fileExists(resizedDirectories[1], fileName)) {
                    BufferedImage img300 = imageUtil.resizeImage(img, 300, 300);
                    Stream.of(dailyDirectories[0], resizedDirectories[1], dailyDirectories[2], resizedDirectories[3])
                            .forEach(directory -> imageUtil.writeImage(img300, Path.of(localDirectory, directory, fileName)));
                }
                if (!commonUtil.fileExists(resizedDirectories[2], fileName)) {
                    BufferedImage img500 = imageUtil.resizeImage(img, 500, 500);
                    Stream.of(resizedDirectories[2], dailyDirectories[1])
                            .forEach(directory -> imageUtil.writeImage(img500, Path.of(localDirectory, directory, fileName)));
                }
            } catch (Exception e) {
                if (e instanceof FileNotFoundException) {
                    // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                    throw e;
                }
                log.debug("Error occurred while resizing {}: {}", fileName, e.getMessage());
                exceptList.add(fileName);
            }
        }
        return exceptList;
    }

}

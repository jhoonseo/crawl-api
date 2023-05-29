package com.project.crawl.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonUtil {

    @Value("${local.directory}")
    private String localDirectory;

    @Value("${local.images.directory}")
    private String localImagesDirectory;

    @Value("${local.daily.directory}")
    private String localDailyDirectory;

    public Boolean checkClassExist(String className, WebDriver driver) {
        try {
            driver.findElement(By.className(className));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;

    }

    public Boolean checkClassFrom(String className, WebElement aFrom) {
        try {
            aFrom.findElement(By.className(className));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public Boolean checkTagFrom(String tagName, WebElement aFrom) {
        try {
            aFrom.findElement(By.tagName(tagName));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public String getCurrentTime() {
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(currentTimeMillis);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(timestamp);
    }

    public Timestamp getCurrentTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        return new Timestamp(currentTimeMillis);
    }

    public boolean fileExists(String directory, String fileName) {
        return Files.exists(Paths.get(directory, fileName));
    }

    public Set<File> getFilteredFileSet(File[] files) {
        return Arrays.stream(files)
                .filter(file -> file.isFile() && !file.isHidden() && !file.getName().startsWith("."))
                .collect(Collectors.toSet());
    }

    public boolean isNukkiImage(String imageUrl) {
        try {
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            int width = image.getWidth();
            int height = image.getHeight();

            // 이미지의 배경 픽셀을 가져옴
            int topLeftPixel = image.getRGB(0, 0);
            int topRightPixel = image.getRGB(width - 1, 0);
            int bottomLeftPixel = image.getRGB(0, height - 1);
            int bottomRightPixel = image.getRGB(width - 1, height - 1);

            // 배경이 흰색인 경우를 누끼 이미지로 간주
            return isPixelWhite(topLeftPixel)
                    && isPixelWhite(topRightPixel)
                    && isPixelWhite(bottomLeftPixel)
                    && isPixelWhite(bottomRightPixel);
        } catch (Exception e) {
            // 이미지를 불러오는 도중 예외가 발생하면 누끼 이미지로 처리하지 않음
            return false;
        }
    }

    public void createDirectory(String directory) throws IOException {
        Path path = Paths.get(directory);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public void generateDirectories(String... directories) throws IOException {
        for (String directory : directories) {
            Path path = Paths.get(directory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }
    }

    public void generateDirectories(Path... paths) throws IOException {
        for (Path path : paths) {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }
    }

    public void generateDailyDirectories(String formatToday) throws IOException {
        String dailyToday = String.join("/", localDailyDirectory, formatToday);
        String dailyTodayImages = String.join("/", localDailyDirectory, formatToday, "images");

        generateDirectories(localDirectory, localImagesDirectory, localDailyDirectory, localDailyDirectory, dailyToday, dailyTodayImages);
    }

    public boolean isImageDownloaded(String imageUrl, String fileName, String formatToday) throws IOException {
        Path filePath = Path.of(localImagesDirectory, fileName);
        if (Files.exists(filePath)) {
            // 이미 파일이 존재하므로 다운로드 또는 복사하지 않고 넘어감
            return true;
        }
        InputStream in;
        try {
            Path dailyImagesPath = Path.of(String.join("/", localDailyDirectory, formatToday, "images", fileName));
            URL url = new URL(imageUrl);
            in = new BufferedInputStream(url.openStream());
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            in = new BufferedInputStream(url.openStream());
            Files.copy(in, dailyImagesPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            return false;
        }
        close(in); // ImageInputStream 제거

        return true;
    }

    private boolean isPixelWhite(int rgb) {
        Color color = new Color(rgb);
        return color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255;
    }

    public void close(ImageInputStream in) {
        try {
            in.close();
        } catch (Exception ignored) {
        }
    }

    public void close(InputStream in) {
        try {
            in.close();
        } catch (Exception ignored) {
        }
    }

}

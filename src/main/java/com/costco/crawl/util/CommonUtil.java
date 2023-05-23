package com.costco.crawl.util;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class CommonUtil {

    @Value("${local.directory}")
    private String localDirectory;

    @Value("${local.directory.images}")
    private String localDirectoryImages;

    @Value("${local.directory.daily}")
    private String localDirectoryDaily;

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

    public static void createDirectory(String directory) throws IOException {
        Path path = Paths.get(directory);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public void generateDirectories(String formatToday) throws IOException {
        String dailyToday = String.join("/", localDirectoryDaily, formatToday);
        String dailyTodayImages = String.join("/", localDirectoryDaily, formatToday, "images");

        createDirectory(localDirectory);
        createDirectory(localDirectoryImages);
        createDirectory(localDirectoryDaily);
        createDirectory(dailyToday);
        createDirectory(dailyTodayImages);
    }

    public boolean isImageDownloaded(String imageUrl, String fileName, String formatToday) {
        Path filePath = Path.of(localDirectoryImages, fileName);
        if (Files.exists(filePath)) {
            // 이미 파일이 존재하므로 다운로드 또는 복사하지 않고 넘어감
            return true;
        }

        try {
            Path dailyImagesPath = Path.of(String.join("/", localDirectoryDaily, formatToday, "images", fileName));
            URL url = new URL(imageUrl);
            InputStream in = new BufferedInputStream(url.openStream());
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);

            in = new BufferedInputStream(url.openStream());
            Files.copy(in, dailyImagesPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean isPixelWhite(int rgb) {
        Color color = new Color(rgb);
        return color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255;
    }

}

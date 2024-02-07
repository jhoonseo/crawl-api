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
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonUtil {

    @Value("${local.directory.costco}")
    private String localDirectoryCostco;

    @Value("${local.images.directory.costco}")
    private String localImagesDirectoryCostco;

    @Value("${local.daily.directory.costco}")
    private String localDailyDirectoryCostco;

    @Value("${local.directory.1688}")
    private String localDirectory1688;

    @Value("${local.images.directory.1688}")
    private String localImagesDirectory1688;
    // localImagesDirectory1688 디렉토리에는 상품 썸네일 및 상품상세 이미지가 모두 저장됨

    @Value("${local.daily.directory.1688}")
    private String localDailyDirectory1688;

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

    public String getTextOfClassFrom(String className, WebElement aFrom) {
        try {
            return aFrom.findElement(By.className(className)).getText();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public String getOuterHTMLFromDriver(String className, WebDriver driver) {
        try {
            return driver.findElement(By.className(className)).getAttribute("outerHTML");
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public WebElement getWebElementByClassFromDriver(String className, WebDriver driver) {
        try {
            return driver.findElement(By.className(className));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public Boolean checkTagFrom(String tagName, WebElement aFrom) {
        try {
            aFrom.findElement(By.tagName(tagName));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public Integer getTagCountFrom(String tagName, WebElement aFrom) {
        try {
            List<WebElement> elements = aFrom.findElements(By.tagName(tagName));
            // 찾은 요소의 갯수를 반환합니다.
            return elements.size();
        } catch (NoSuchElementException e) {
            // 요소가 없으면 0을 반환합니다.
            return 0;
        }
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

    public void generateDailyDirectoriesCostco(String formatToday) throws IOException {
        String dailyToday = String.join("/", localDailyDirectoryCostco, formatToday);
        String dailyTodayImages = String.join("/", localDailyDirectoryCostco, formatToday, "images");

        generateDirectories(localDirectoryCostco, localImagesDirectoryCostco, localDailyDirectoryCostco, localDailyDirectoryCostco, dailyToday, dailyTodayImages);
    }

    public void generateDailyDirectories1688(String formatToday) throws IOException {
        String dailyToday = String.join("/", localDailyDirectory1688, formatToday);
        String dailyTodayImages = String.join("/", localDailyDirectory1688, formatToday, "images");

        generateDirectories(localDirectory1688, localImagesDirectory1688, localDailyDirectory1688, localDailyDirectory1688, dailyToday, dailyTodayImages);
    }

    public void generateTranslateDirectoryKo1688(String formatToday) throws IOException {
        String koDirectory = String.join("/", localDailyDirectory1688, formatToday, "ko");
        generateDirectories(koDirectory);
    }

    public boolean isImageDownloadedCostco(String imageUrl, String formatToday) {
        String fileName = imageUrl.split("/")[imageUrl.split("/").length - 1];
        return isImageDownloadedCostco(imageUrl, fileName, formatToday);
    }

    public boolean isImageDownloadedCostco(String imageUrl, String fileName, String formatToday) {
        Path filePath = Path.of(localImagesDirectoryCostco, fileName);
        if (Files.exists(filePath)) {
            // 이미 파일이 존재하므로 다운로드 또는 복사하지 않고 넘어감
            return true;
        }
        InputStream in;
        try {
            Path dailyImagesPath = Path.of(String.join("/", localDailyDirectoryCostco, formatToday, "images", fileName));
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

    public boolean isImageDownloaded1688(String imageUrl, String fileName, String formatToday) {
        Path filePath = Path.of(localImagesDirectory1688, fileName);
        if (Files.exists(filePath)) {
            // 이미 파일이 존재하므로 다운로드 또는 복사하지 않고 넘어감
            return true;
        }
        InputStream in;
        try {
            Path dailyImagesPath = Path.of(String.join("/", localDailyDirectory1688, formatToday, "images", fileName));
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

    public boolean isContainingChinese(String text) {
        // Regular expression to detect Chinese characters
        Pattern chinesePattern = Pattern.compile("\\p{IsHan}+");
        return chinesePattern.matcher(text).find();
    }

    public double calcChineseCharactersRatio(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0; // 텍스트가 null이거나 비어있는 경우, 비율은 0으로 정의합니다.
        }

        // 특수 문자를 제외한 모든 글자들만 추출합니다.
        String lettersOnly = text.replaceAll("[^\\p{L}\\p{Nd}]+", "");

        // 중국어 문자를 감지하기 위한 정규 표현식
        Pattern chinesePattern = Pattern.compile("\\p{IsHan}+");
        Matcher matcher = chinesePattern.matcher(lettersOnly);

        int chineseCharactersCount = 0;

        // 일치하는 모든 중국어 문자의 개수를 계산합니다.
        while (matcher.find()) {
            chineseCharactersCount += matcher.group().length();
        }

        // lettersOnly 문자열 길이 대비 중국어 문자의 비율을 계산합니다.
        // lettersOnly 문자열은 특수 문자를 제외한 글자들만 포함합니다.
        double ratio = lettersOnly.isEmpty() ? 0.0 : (double) chineseCharactersCount / lettersOnly.length();

        return ratio; // 비율을 반환합니다.
    }

    public boolean downloadImage(String imageUrl, String formatToday) {
        String fileName = imageUrl.split("/")[imageUrl.split("/").length - 1];
        Path filePath = Path.of(localImagesDirectoryCostco, fileName);

        InputStream in;
        try {
            Path dailyImagesPath = Path.of(String.join("/", localDailyDirectoryCostco, formatToday, "images", fileName));
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

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
import java.util.HashSet;
import java.util.List;
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

    public String convertListToString(List<Integer> list) {
        return list.stream() // 리스트를 스트림으로 변환
                .map(String::valueOf) // 각 정수를 문자열로 변환
                .collect(Collectors.joining("|")); // 문자열을 '|'로 구분하여 결합
    }

    public List<String> convertStringToList(String str) {
        // 문자열을 '|' 기준으로 나누고, 결과를 List<String>으로 반환
        return Arrays.asList(str.split("\\|"));
    }

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

    public String getFilenameFromUrl(String imgUrl) {
        String[] parts = imgUrl.split("/");
        return parts[parts.length - 1]; // URL의 마지막 부분(파일명)을 반환
    }

    public Set<String> getSortedFilenameSet(String sourceDirectory, String targetDirectory) throws IOException {
        Set<String> sourceFileNames = Files.list(Paths.get(sourceDirectory))
                .map(path -> path.getFileName().toString()).collect(Collectors.toSet());

        // 대상 디렉토리의 모든 파일명을 메모리에 로드
        Set<String> existingFileNames = Files.list(Paths.get(targetDirectory))
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toSet());

        Set<String> newFileNameSet = new HashSet<>();

        for (String fileName : sourceFileNames) {
            // 이미 파일이 대상 디렉토리에 존재하는지 확인
            if (!existingFileNames.contains(fileName)) {
                // 파일이 존재하지 않는 경우에 newFileNameSet 에 넣음
                newFileNameSet.add(fileName);
            }
        }
        return newFileNameSet;

    }


    public void downloadImages1688(List<String> imageUrlList, String formatToday) throws IOException {
        // 중복된 이미지 URL 제거
        Set<String> distinctImageUrlSet = new HashSet<>(imageUrlList);

        try {
            // 로컬 이미지 디렉토리의 모든 파일명을 메모리에 로드
            Set<String> existingFileNames = Files.list(Path.of(localImagesDirectory1688))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());

            for (String imageUrl : distinctImageUrlSet) {
                String fileName = imageUrl.split("/")[imageUrl.split("/").length - 1];
                // 이미 파일이 존재하는지 확인
                if (!existingFileNames.contains(fileName)) {
                    // 파일이 존재하지 않는 경우에만 다운로드
                    if (!downloadAndCopyImage(imageUrl, localImagesDirectory1688, localDailyDirectory1688, formatToday)) {
                        // 다운로드에 실패한 경우
                        System.err.println("Failed to download image: " + imageUrl);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    // 공통 다운로드 및 복사 로직을 처리하는 메소드
    private boolean downloadAndCopyImage(String imageUrl, String localImagesDirectory, String localDailyDirectory, String formatToday) {
        String fileName = imageUrl.split("/")[imageUrl.split("/").length - 1];
        Path filePath = Path.of(localImagesDirectory, fileName);

        try (InputStream in = new BufferedInputStream(new URL(imageUrl).openStream())) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING); // 로컬 이미지 디렉토리에 저장
            Path dailyImagesPath = Path.of(localDailyDirectory, formatToday, "images").resolve(fileName);
            Files.createDirectories(dailyImagesPath.getParent()); // 필요한 모든 부모 디렉토리를 생성
            Files.copy(filePath, dailyImagesPath, StandardCopyOption.REPLACE_EXISTING); // 일일 이미지 디렉토리에 복사
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    // 공통 다운로드 및 복사 로직을 처리하는 메소드
    private boolean downloadAndCopyImageAfterExistenceCheck(String imageUrl, String localImagesDirectory, String localDailyDirectory, String formatToday) {
        String fileName = imageUrl.split("/")[imageUrl.split("/").length - 1];
        Path filePath = Path.of(localImagesDirectory, fileName);
        if (Files.exists(filePath)) {
            return true; // 이미 파일이 존재하므로 다운로드 또는 복사하지 않고 넘어감
        }
        try (InputStream in = new BufferedInputStream(new URL(imageUrl).openStream())) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING); // 로컬 이미지 디렉토리에 저장

            Path dailyImagesPath = Path.of(localDailyDirectory, formatToday, "images", fileName);
            Files.createDirectories(dailyImagesPath.getParent()); // 필요한 모든 부모 디렉토리를 생성
            Files.copy(in, dailyImagesPath, StandardCopyOption.REPLACE_EXISTING); // 일일 이미지 디렉토리에 복사
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean downloadImage1688(String imageUrl, String formatToday) {
        return downloadAndCopyImageAfterExistenceCheck(imageUrl, localImagesDirectory1688, localDailyDirectory1688, formatToday);
    }

    public boolean isImageDownloadedCostco(String imageUrl, String formatToday) {
        return downloadAndCopyImageAfterExistenceCheck(imageUrl, localImagesDirectoryCostco, localDailyDirectoryCostco, formatToday);
    }

    public boolean isImageDownloaded1688(String imageUrl, String formatToday) {
        return downloadAndCopyImageAfterExistenceCheck(imageUrl, localImagesDirectory1688, localDailyDirectory1688, formatToday);
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

        return lettersOnly.isEmpty() ? 0.0 : (double) chineseCharactersCount / lettersOnly.length(); // 비율을 반환합니다.
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

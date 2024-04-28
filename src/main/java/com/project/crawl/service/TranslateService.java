package com.project.crawl.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    @Value("${ncp.client.id}")
    private String clientId;

    @Value("${ncp.client.secret}")
    private String clientSecret;


    public void translateImage(String imagePath, String outputImagePath, String copyPath) {
        String targetLanguage = "ko"; // 번역할 대상 언어
        String sourceLanguage = "zh-CN"; // 원본 이미지의 언어
        try {
            String apiUrl = "https://naveropenapi.apigw.ntruss.com/image-to-image/v1/translate"; // API URL
            File imageFile = new File(imagePath);
            String boundary = UUID.randomUUID().toString(); // Boundary 생성
            String crlf = "\r\n"; // 줄바꿈
            String twoHyphens = "--"; // Boundary 시작을 위한 구분자
            String charset = "UTF-8"; // 문자 인코딩

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId); // 클라이언트 ID
            connection.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret); // 클라이언트 Secret

            try (OutputStream output = connection.getOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true)) {
                // 소스 언어 설정 파라미터 추가
                writer.append(twoHyphens).append(boundary).append(crlf);
                writer.append("Content-Disposition: form-data; name=\"source\"").append(crlf);
                writer.append("Content-Type: text/plain; charset=").append(charset).append(crlf);
                writer.append(crlf).append(sourceLanguage).append(crlf).flush();

                // 타겟 언어 설정 파라미터 추가
                writer.append(twoHyphens).append(boundary).append(crlf);
                writer.append("Content-Disposition: form-data; name=\"target\"").append(crlf);
                writer.append("Content-Type: text/plain; charset=").append(charset).append(crlf);
                writer.append(crlf).append(targetLanguage).append(crlf).flush();

                // 이미지 파일 파라미터 추가
                writer.append(twoHyphens).append(boundary).append(crlf);
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(imageFile.getName()).append("\"").append(crlf);
                writer.append("Content-Type: ").append(Files.probeContentType(imageFile.toPath())).append(crlf); // 이미지 파일 타입
                writer.append(crlf).flush();
                Files.copy(imageFile.toPath(), output); // 이미지 파일 데이터 전송

                output.flush();
                writer.append(crlf).flush();

                // 마지막 boundary 추가
                writer.append(twoHyphens).append(boundary).append(twoHyphens).append(crlf).flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // 응답 데이터 파싱 및 이미지 저장
                    String responseData = response.toString();
                    String imageBase64 = responseData.split("\"renderedImage\":\"")[1].split("\"")[0]; // 응답 구조에 따라 조정 필요
                    byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

                    Path outputPath = Paths.get(outputImagePath);
                    Files.write(outputPath, imageBytes); // 번역된 이미지 파일 저장

                    // download 한 파일을 copy 진행
                    if (!copyPath.isEmpty()) {
                        Files.copy(outputPath, Path.of(copyPath), StandardCopyOption.REPLACE_EXISTING); // 일일 이미지 디렉토리에 복사
                    }
                }
            } else {
                log.error("Error: " + responseCode);
            }
        } catch (IOException e) {
            log.error("Exception occurred during image translation", e);
        }
    }

    public void translateImages(String sourceDirectory, String targetDirectory, String copyPath) throws IOException {
        // 원본 디렉토리에서 파일명 추출
        Set<String> sourceFileNames = Files.list(Paths.get(sourceDirectory))
                .map(path -> path.getFileName().toString()).collect(Collectors.toSet());

        // 대상 디렉토리의 모든 파일명을 메모리에 로드
        Set<String> existingFileNames = Files.list(Paths.get(targetDirectory))
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toSet());

        for (String fileName : sourceFileNames) {
            String imagePath = Paths.get(sourceDirectory, fileName).toString();
            String outputImagePath = Paths.get(targetDirectory, fileName).toString();

            // 이미 파일이 대상 디렉토리에 존재하는지 확인
            if (!existingFileNames.contains(fileName)) {
                // 파일이 존재하지 않는 경우에만 번역
                translateImage(imagePath, outputImagePath, copyPath);
            }
        }
    }

    public String getDetectLanguageCodeNCP(String text) {
        try {
            String apiUrl = "https://naveropenapi.apigw.ntruss.com/langs/v1/dect";
            HttpURLConnection connection = prepareConnection(apiUrl);

            // POST 요청에 필요한 파라미터 전송
            sendPostData(connection, "query=" + URLEncoder.encode(text, StandardCharsets.UTF_8));

            // 응답 처리
            return readResponse(connection);
        } catch (Exception e) {
            log.error("Error detecting language", e);
            return null;
        }
    }

    public String translateText(String originalText, String sourceLang, String targetLang) {
        try {
            String apiUrl = "https://naveropenapi.apigw.ntruss.com/nmt/v1/translation";
            HttpURLConnection connection = prepareConnection(apiUrl);

            // POST 요청에 필요한 파라미터 전송
            sendPostData(connection, "source=" + sourceLang + "&target=" + targetLang + "&text=" + URLEncoder.encode(originalText, StandardCharsets.UTF_8));

            // 응답 처리
            return readResponse(connection);
        } catch (Exception e) {
            log.error("Error translating text", e);
            return null;
        }
    }

    private HttpURLConnection prepareConnection(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
        con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
        return con;
    }

    private String readResponse(HttpURLConnection con) throws IOException {
        int responseCode = con.getResponseCode();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode == 200 ? con.getInputStream() : con.getErrorStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private void sendPostData(HttpURLConnection connection, String postData) throws IOException {
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.writeBytes(postData);
            wr.flush();
        }
    }

    public String getDetectedTextFromImage(String imagePath) {
        StringBuilder detectedTextBuilder = new StringBuilder();
        List<AnnotateImageRequest> requests = new ArrayList<>();

        try {
            ByteString imgBytes = ByteString.readFrom(new FileInputStream(imagePath));

            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            requests.add(request);

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        log.error("Error: {}", res.getError().getMessage());
                        return null;
                    }

                    for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                        detectedTextBuilder.append(annotation.getDescription()).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Exception occurred during image text detection", e);
            return null;
        }

        return detectedTextBuilder.toString();
    }
}

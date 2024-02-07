package com.project.crawl.service;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    @Value("${ncp.client.id}")
    private String clientId;

    @Value("${ncp.client.secret}")
    private String clientSecret;


    public void translateImage(String imagePath, String outputImagePath) {
        String targetLanguage = "ko";
        String sourceLanguage = "zh-CN";
        try {
            String apiUrl = "https://naveropenapi.apigw.ntruss.com/image-to-image/v1/translate";

            File imageFile = new File(imagePath);
            String boundary = UUID.randomUUID().toString();
            String crlf = "\r\n";
            String twoHyphens = "--";
            String charset = "UTF-8";

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-searchData; boundary=" + boundary);
            connection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            connection.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);

            try (OutputStream output = connection.getOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true)) {
                // 이미지와 언어 설정 파라미터 추가
                writer.append(twoHyphens + boundary).append(crlf);
                writer.append("Content-Disposition: form-searchData; name=\"source\"").append(crlf);
                writer.append("Content-Type: text/plain; charset=" + charset).append(crlf);
                writer.append(crlf).append(sourceLanguage).append(crlf).flush();

                writer.append(twoHyphens + boundary).append(crlf);
                writer.append("Content-Disposition: form-searchData; name=\"target\"").append(crlf);
                writer.append("Content-Type: text/plain; charset=" + charset).append(crlf);
                writer.append(crlf).append(targetLanguage).append(crlf).flush();

                writer.append(twoHyphens + boundary).append(crlf);
                writer.append("Content-Disposition: form-searchData; name=\"image\"; filename=\"" + imageFile.getName() + "\"").append(crlf);
                writer.append("Content-Type: " + Files.probeContentType(imageFile.toPath())).append(crlf);
                writer.append(crlf).flush();
                Files.copy(imageFile.toPath(), output);
                output.flush();
                writer.append(crlf).flush();

                writer.append(twoHyphens + boundary + "--").append(crlf).flush();
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
                    Files.write(outputPath, imageBytes);
                }

            } else {
                log.error("Error: " + responseCode);
            }
        } catch (IOException e) {
            log.error("Exception occurred during image translation", e);
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

    public String getDetectLanguageCodeNCP(String text) {
        try {
            String query = URLEncoder.encode(text, "UTF-8");
            String apiURL = "https://naveropenapi.apigw.ntruss.com/langs/v1/dect"; // 올바른 API 주소는 "https://openapi.naver.com/v1/papago/detectLangs" 입니다.
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);

            // POST 요청에 필요한 파라미터 전송
            String postParams = "query=" + query;
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(postParams);
                wr.flush();
            }

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 오류 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 예외 발생 시 null 반환
        }
    }

    // 텍스트 번역 메서드
    public String translateText(String originalText, String sourceLang, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(originalText, "UTF-8");
            String apiURL = "https://naveropenapi.apigw.ntruss.com/nmt/v1/translation";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);

            // POST 요청에 필요한 파라미터
            String postParams = "source=" + sourceLang + "&target=" + targetLang + "&text=" + encodedText;
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(postParams);
                wr.flush();
            }

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 오류 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            // 번역 결과 파싱 (여기서는 단순화를 위해 전체 응답을 출력하고 있습니다. 실제로는 JSON 파싱이 필요합니다.)
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null; // 예외 발생 시 null 반환
        }
    }

}

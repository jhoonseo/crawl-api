import java.nio.file.Path;

public class PathTest {
    static String path = "/Volumes/SSD_T7/costco_test/images";
    static String fileName = "abc.jpg";
    static String cdnBaseUrl = "https://d2t4m7vag2t4p6.cloudfront.net/images";

    public static void main(String[] args) {
        Path filePath = Path.of(path, fileName);
        System.out.println(filePath);
        String src = String.join("/", cdnBaseUrl, fileName);
        System.out.println(src);
    }
}

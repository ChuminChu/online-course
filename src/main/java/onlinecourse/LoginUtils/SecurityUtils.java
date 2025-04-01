package onlinecourse.LoginUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

public class SecurityUtils {

    private static final MessageDigest SHA256;

    static {
        try {
            SHA256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없음");
        }
    }

    public static String sha256EncryptBase64(String plainText) {
        byte[] hash = SHA256.digest(plainText.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    // Java 17 미만일 경우
    public static String sha256EncryptHex1(String plainText) {
        byte[] hash = SHA256.digest(plainText.getBytes());
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hash) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }

    // Java 17 이상에서 사용 가능한 방법
    public static String sha256EncryptHex2(String plainText) {
        byte[] hash = SHA256.digest(plainText.getBytes());
        return HexFormat.of().formatHex(hash);
    }
}
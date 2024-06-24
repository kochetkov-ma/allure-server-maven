package brewcode.org.allure;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

class Utilities {
    private Utilities() throws NoSuchAlgorithmException {
        throw new IllegalStateException("Utility class");
    }

    public static final ObjectMapper MAPPER = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    public static final SSLContext sslContext;

    static {
        SSLContext sslContextTmp;
        try {
            sslContextTmp = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Failed to create non strict SSL context: " + e.getMessage());
            sslContextTmp = null;
        }

        sslContext = sslContextTmp;
        TrustManager[] trustAllCertificates = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        if (sslContext != null)
            try {
                sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            } catch (KeyManagementException e) {
                System.err.println("Failed to initialize non strict SSL context: " + e.getMessage());
            }
    }

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final HttpClient clientSkipSSL = HttpClient.newBuilder()
        .sslContext(sslContext)
        .build();

    public static HttpClient getHttpClient(boolean skipSSL) {
        if (sslContext != null && skipSSL)
            return clientSkipSSL;
        else
            return client;
    }

    public static HttpRequest.BodyPublisher buildMultipartData(Path archiveResultPath, String boundary) throws IOException {

        var fileBytes = Files.readAllBytes(archiveResultPath);
        var CRLF = "\r\n";

        String preFileContent = "--" + boundary + CRLF +
            "Content-Disposition: form-data; name=\"allureResults\"; filename=\"" + archiveResultPath.getFileName().toString() + "\"" + CRLF +
            "Content-Type: application/zip" + CRLF + CRLF;
        String postFileContent = CRLF + "--" + boundary + "--" + CRLF;

        return HttpRequest.BodyPublishers.ofByteArrays(
            List.of(
                preFileContent.getBytes(),
                fileBytes,
                postFileContent.getBytes()
            )
        );
    }

}

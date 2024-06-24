package brewcode.org.allure;

import brewcode.org.allure.action.GitLabMRCommentAction;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import static brewcode.org.allure.Utilities.MAPPER;
import static brewcode.org.allure.Utilities.buildMultipartData;
import static brewcode.org.allure.Utilities.getHttpClient;

@Mojo(name = "allure-server-generate", defaultPhase = LifecyclePhase.SITE)
public class ReportGenerateMojo extends AbstractMojo {

    private static final String UPLOADED_RESULT_UUID = "uploaded-result-uuid.txt";
    private static final String GENERATED_REPORT_URL = "generated-report-url.txt";

    public ReportGenerateMojo() {
    }

    ReportGenerateMojo(String allureServerUrl, String resultUuidFile, String reportUuidFile, boolean giLabCallback, String folderName, String outputPath, MavenProject project) {
        this.allureServerUrl = allureServerUrl;
        this.resultUuidFile = resultUuidFile;
        this.reportUuidFile = reportUuidFile;
        this.giLabCallback = giLabCallback;
        this.folderName = folderName;
        this.outputPath = outputPath;
        this.project = project;
    }

    //// SERVER ////

    @Parameter(property = "allureServerUrl", required = true)
    private String allureServerUrl;

    @Parameter(property = "resultUuidFile", defaultValue = "${project.build.directory}/" + UPLOADED_RESULT_UUID)
    private String resultUuidFile;

    @Parameter(property = "resultUuidFile", defaultValue = "${project.build.directory}/" + GENERATED_REPORT_URL)
    private String reportUuidFile;

    @Parameter(property = "giLabCallback", defaultValue = "false")
    private boolean giLabCallback;

    //// ARCHIVE ////

    @Parameter(property = "folderName", defaultValue = "allure-results")
    private String folderName;

    @Parameter(property = "outputPath", defaultValue = "${project.build.directory}/archives/allure-results.zip")
    private String outputPath;

    //// MAVEN ////

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    //// PRIVATE ////

    private final HttpClient httpClient = getHttpClient(true);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void execute() throws MojoExecutionException {
        var archiveMojo = new ArchiveMojo(folderName, outputPath, project);
        archiveMojo.execute();

        var archiveResultPath = Paths.get(outputPath);

        if (!Files.exists(archiveResultPath))
            throw new MojoExecutionException("Allure result archive '" + archiveResultPath + "' does not exist.");

        try {
            if (Files.size(archiveResultPath) == 0)
                throw new MojoExecutionException("Allure result archive '" + archiveResultPath + "' is empty.");
        } catch (IOException e) {
            throw new RuntimeException("Cannot check archive size " + archiveResultPath, e);
        }

        getLog().info("Sending results '" + archiveResultPath + "' to server '" + allureServerUrl + "'");

        try {
            var resultUuid = upload(archiveResultPath);
            Files.writeString(Paths.get(resultUuidFile), resultUuid);

            var reportUrl = generate(resultUuid);
            Files.writeString(Paths.get(reportUuidFile), reportUrl);
            getLog().info("Report has been generated: '" + reportUrl + "' . And saved to file '" + resultUuidFile + "' [SUCCESS]");

            if (giLabCallback) {
                getLog().info("GitLab callback enabled");
                GitLabMRCommentAction.create(httpClient, getLog())
                        .execute(reportUrl);
            } else getLog().info("GitLab callback disabled");
            
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Failed to send results to server", e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String upload(Path archiveResultPath) throws IOException, InterruptedException {

        String boundary = UUID.randomUUID().toString();
        var body = buildMultipartData(archiveResultPath, boundary);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(allureServerUrl + "/api/result"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(body)
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        getLog().info("Allure Server '/api/result' response: " + response);
        return getResponseMap(response).get("uuid");
    }

    private String generate(String uuid) throws IOException, InterruptedException {
        var body = Requests.reportGenerateJsonRequest(uuid);
        var bodyPublisher = HttpRequest.BodyPublishers.ofString(body);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(allureServerUrl + "/api/report"))
                .header("Content-Type", "application/json")
                .POST(bodyPublisher)
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        getLog().info("Allure Server '/api/report' response: " + response);
        return getResponseMap(response).get("url");
    }

    private Map<String, String> getResponseMap(HttpResponse<String> response) throws IOException {
        return MAPPER.readValue(response.body(), new TypeReference<>() {
        });
    }
}
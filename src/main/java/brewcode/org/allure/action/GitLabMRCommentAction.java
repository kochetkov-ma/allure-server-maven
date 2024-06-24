package brewcode.org.allure.action;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GitLabMRCommentAction {

    private final HttpClient httpClient;
    private final Log log;
    private final String gitLabApiUrl;
    private final String gitLabProjectId;
    private final String gitLabMergeRequestId;
    private final String gitLabToken;

    GitLabMRCommentAction(HttpClient httpClient, Log log, String gitLabApiUrl, String gitLabProjectId, String gitLabMergeRequestId, String gitLabToken) {
        this.httpClient = httpClient;
        this.log = log;
        this.gitLabApiUrl = gitLabApiUrl;
        this.gitLabProjectId = gitLabProjectId;
        this.gitLabMergeRequestId = gitLabMergeRequestId;
        this.gitLabToken = gitLabToken;
    }

    public static GitLabMRCommentAction create(HttpClient httpClient, Log log) {
        return new GitLabMRCommentAction(
            httpClient,
            log,
            System.getenv("CI_API_V4_URL"),
            System.getenv("CI_PROJECT_ID"),
            System.getenv("CI_MERGE_REQUEST_IID"),
            System.getenv("SERVICE_USER_API_TOKEN")
        );
    }

    public void execute(String reportUrl) throws MojoExecutionException {
        log.info("Sending report URL to GitLab Merge Request...");

        if (!checkEnvVars()) {
            log.error("GitLab callback wont be executed because of missing environment variables.");
            return;
        }

        var gitLabUrl = gitLabApiUrl + "/projects/" + gitLabProjectId + "/merge_requests/" + gitLabMergeRequestId + "/notes";

        var request = HttpRequest.newBuilder()
            .uri(URI.create(gitLabUrl))
            .header("Content-Type", "application/json")
            .header("Private-Token", gitLabToken)
            .POST(HttpRequest.BodyPublishers.ofString("ALLURE REPORT: " + reportUrl))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("GitLab response: " + response.statusCode());
            log.info("Report URL successfully sent to GitLab Merge Request.");
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Failed to send report URL to GitLab Merge Request", e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean checkEnvVars() {
        var isValid = true;

        if (gitLabMergeRequestId == null || gitLabMergeRequestId.isBlank()) {
            log.error("CI_MERGE_REQUEST_IID is not set");
            isValid = false;
        }

        if (gitLabProjectId == null || gitLabProjectId.isBlank()) {
            log.error("CI_PROJECT_ID is not set");
            isValid = false;
        }

        if (gitLabApiUrl == null || gitLabApiUrl.isBlank()) {
            log.error("CI_API_V4_URL is not set");
            isValid = false;
        }

        if (gitLabToken == null || gitLabToken.isBlank()) {
            log.error("SERVICE_USER_API_TOKEN is not set");
            isValid = false;
        }

        return isValid;
    }
}
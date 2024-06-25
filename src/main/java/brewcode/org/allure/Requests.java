package brewcode.org.allure;

import brewcode.org.allure.model.AllureReportRequest;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

import static brewcode.org.allure.Utilities.MAPPER;

class Requests {

    public static String reportGenerateJsonRequest(String uuid) {

        var reportPrefix = System.getenv("PATH_PREFIX");
        var reportPostfix = System.getenv("PATH_POSTFIX");

        var pipelineId = System.getenv("CI_PIPELINE_ID") != null ? System.getenv("CI_PIPELINE_ID") : "0";
        var pipelineUrl = System.getenv("CI_PIPELINE_URL") != null ? System.getenv("CI_PIPELINE_URL") : "localhost";
        var jobName = System.getenv("CI_JOB_NAME") != null ? System.getenv("CI_JOB_NAME") : "manual";
        var mr = System.getenv("CI_MERGE_REQUEST_IID");
        var branch = System.getenv("CI_COMMIT_REF_NAME") != null ? System.getenv("CI_COMMIT_REF_NAME") : "master";

        var path = new StringBuilder();

        if (reportPrefix != null)
            path.append(reportPrefix);

        if (mr != null)
            path.append(mr.trim());
        else
            path.append(branch);

        path.append("/").append(jobName);

        if (reportPostfix != null)
            path.append("/").append(reportPostfix);

        var request = new AllureReportRequest(
            new AllureReportRequest.ReportSpec(List.of(path.toString()), new AllureReportRequest.ReportSpec.ExecutorInfo("GitLab CI", "GitLab CI", pipelineId, pipelineUrl, jobName)),
            List.of(uuid),
            true
        );

        try {
            return MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization error", e);
        }
    }
}
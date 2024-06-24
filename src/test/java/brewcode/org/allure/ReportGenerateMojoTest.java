package brewcode.org.allure;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

class ReportGenerateMojoTest {

    @Test
    void execute() throws MojoExecutionException {
        var mojo = new ReportGenerateMojo(
            "http://localhost:8080",
            "uploaded-result-uuid.txt",
            "generated-report-url.txt",
            false,
            "resources/allure-results",
            "build/archives/allure-results.zip",
            null
        );

        mojo.execute();
    }
}
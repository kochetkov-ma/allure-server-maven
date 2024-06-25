package brewcode.org.allure.action;

import brewcode.org.allure.Utilities;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.jupiter.api.Test;

class GitLabMRCommentActionTest {

    @Test
    void execute() throws MojoExecutionException {
        var gl = new GitLabMRCommentAction(
            Utilities.getHttpClient(true),
            new DefaultLog(new ConsoleLogger()),
            "https://gitlab.com/api/v4",
            "allure-server-maven",
            "1",
            "TOKEN_HERE"
        );

        gl.execute("https://allure.com/ui");

    }
}
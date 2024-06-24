package brewcode.org.allure.model;

import java.util.List;


public class AllureReportRequest {
    private final ReportSpec reportSpec;
    private final List<String> results;
    private final boolean deleteResults;

    public AllureReportRequest(ReportSpec reportSpec, List<String> results, boolean deleteResults) {
        this.reportSpec = reportSpec;
        this.results = results;
        this.deleteResults = deleteResults;
    }

    public static class ReportSpec {
        private final List<String> path;
        private final ExecutorInfo executorInfo;

        public ReportSpec(List<String> path, ExecutorInfo executorInfo) {
            this.path = path;
            this.executorInfo = executorInfo;
        }

        public static class ExecutorInfo {
            private final String name;
            private final String type;
            private final String buildName;
            private final String buildUrl;
            private final String reportName;

            public ExecutorInfo(String name, String type, String buildName, String buildUrl, String reportName) {
                this.name = name;
                this.type = type;
                this.buildName = buildName;
                this.buildUrl = buildUrl;
                this.reportName = reportName;
            }
        }
    }
}
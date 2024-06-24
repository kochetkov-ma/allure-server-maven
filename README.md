Maven plugin for Allure-Server
---

![GitHub release (latest by date)](https://img.shields.io/github/v/release/kochetkov-ma/allure-server-maven)

![Maven Central Version](https://img.shields.io/maven-central/v/org.brewcode.allure/allure-server-maven)

This plugin allows you to deploy Allure reports to Allure-Server.

- [allure-server](https://github.com/kochetkov-ma/allure-server)

Also, there is Gradle Plugin:
- [allure-server-gradle](https://github.com/kochetkov-ma/allure-server-gradle)

Дополненная инструкция по использованию Maven плагина для генерации отчетов Allure, включая информацию о GitLab Callback:

### Allure Maven Plugin

The `allure-server-generate` Maven plugin facilitates the generation and upload of Allure test reports to a specified server. Follow these steps to configure and use the plugin effectively:

#### Get Started

1. **Add Plugin Declaration to pom.xml:**

   Include the following plugin declaration in the `<build><plugins>` section of your `pom.xml` file:

   ```xml
   <plugin>
       <groupId>brewcode.org.allure</groupId>
       <artifactId>allure-maven-plugin</artifactId>
       <version>0.1.0</version>
       <executions>
           <execution>
               <goals>
                   <goal>allure-server-generate</goal>
               </goals>
               <phase>site</phase>
           </execution>
       </executions>
       <configuration>
           <!-- Add configuration options here -->
           <allureServerUrl>http://your-allure-server-url</allureServerUrl>  <!-- Required -->
           <resultUuidFile>${project.build.directory}/uploaded-result-uuid.txt</resultUuidFile> <!-- Optional -->
           <reportUuidFile>${project.build.directory}/generated-report-url.txt</reportUuidFile> <!-- Optional -->
           <giLabCallback>false</giLabCallback> <!-- Enable GitLab Callback --> <!-- Optional -->
           <folderName>allure-results</folderName> <!-- Optional -->
           <outputPath>${project.build.directory}/archives/allure-results.zip</outputPath> <!-- Optional -->
       </configuration>
   </plugin>
   ```

2. **Configure Plugin Options:**

   Customize the plugin behavior by configuring the following parameters:

    - **`allureServerUrl`**: URL of the Allure server where reports will be uploaded and generated.
    - **`resultUuidFile`**: File path to store the UUID of uploaded results.
    - **`reportUuidFile`**: File path to store the UUID of the generated report URL.
    - **`giLabCallback`**: Enable GitLab Callback for automatic comment addition to Merge Requests (ensure GitLab environment variables are set).
    - **`folderName`**: Name of the folder containing Allure results.
    - **`outputPath`**: Path to output the archive of Allure results.

### Configuration Options

#### `allureServerUrl`

- **Description:** URL of the Allure server where test results will be uploaded and reports generated.
- **Example:**
  ```xml
  <allureServerUrl>http://your-allure-server-url</allureServerUrl>
  ```

#### `resultUuidFile`

- **Description:** Path to the file where the UUID of uploaded test results will be stored.
- **Default:** `${project.build.directory}/uploaded-result-uuid.txt`
- **Example:**
  ```xml
  <resultUuidFile>${project.build.directory}/uploaded-result-uuid.txt</resultUuidFile>
  ```

#### `reportUuidFile`

- **Description:** Path to the file where the UUID of the generated report URL will be stored.
- **Default:** `${project.build.directory}/generated-report-url.txt`
- **Example:**
  ```xml
  <reportUuidFile>${project.build.directory}/generated-report-url.txt</reportUuidFile>
  ```

#### `giLabCallback`

- **Description:** Option to enable GitLab Callback for automatically adding comments to GitLab Merge Requests.
- **Default:** `false`
- **Example:**
  ```xml
  <giLabCallback>true</giLabCallback>
  ```

#### `folderName`

- **Description:** Name of the folder where Allure test results are stored.
- **Default:** `allure-results`
- **Example:**
  ```xml
  <folderName>my-allure-results</folderName>
  ```

#### `outputPath`

- **Description:** Path to store the archive containing Allure test results.
- **Default:** `${project.build.directory}/archives/allure-results.zip`
- **Example:**
  ```xml
  <outputPath>${project.build.directory}/my-allure-reports/allure-results.zip</outputPath>
  ```

#### GitLab Callback Integration

GitLab Callback automatically adds comments to GitLab Merge Requests with links to generated Allure reports. To use this feature:

1. **Set Environment Variables in GitLab CI/CD:**

   Ensure the following environment variables are set in your GitLab project's CI/CD pipeline settings:

    - `CI_API_V4_URL`: URL of the GitLab API.
    - `CI_PROJECT_ID`: ID of your GitLab project.
    - `CI_MERGE_REQUEST_IID`: ID of the Merge Request in GitLab.
    - `SERVICE_USER_API_TOKEN`: API token for accessing GitLab.

   These variables are necessary for GitLab Callback to function correctly and are typically provided automatically by GitLab during pipeline execution.

2. **Configure Maven Plugin for GitLab Callback:**

   Add the `giLabCallback` parameter to your Maven plugin configuration in `pom.xml` as shown in the example above. This setting enables the plugin to use GitLab environment variables for callback functionality.

### Example Configuration in pom.xml

```xml
<build>
    <plugins>
        <plugin>
            <groupId>brewcode.org.allure</groupId>
            <artifactId>allure-maven-plugin</artifactId>
            <version>0.1.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>allure-server-generate</goal>
                    </goals>
                    <phase>site</phase>
                </execution>
            </executions>
            <configuration>
                <allureServerUrl>http://your-allure-server-url</allureServerUrl>
                <resultUuidFile>${project.build.directory}/uploaded-result-uuid.txt</resultUuidFile>
                <reportUuidFile>${project.build.directory}/generated-report-url.txt</reportUuidFile>
                <giLabCallback>true</giLabCallback>
                <folderName>allure-results</folderName>
                <outputPath>${project.build.directory}/archives/allure-results.zip</outputPath>
            </configuration>
        </plugin>
    </plugins>
</build>
```

This comprehensive guide provides detailed instructions on setting up and using your Maven plugin for Allure report generation, along with enabling GitLab Callback for seamless integration with GitLab CI/CD environments. Adjust configuration options and ensure environment variables are correctly set to maximize the plugin's functionality.
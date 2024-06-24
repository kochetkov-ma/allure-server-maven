package brewcode.org.allure;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Mojo(name = "allure-results-archive", defaultPhase = LifecyclePhase.NONE)
public class ArchiveMojo extends AbstractMojo {

    public ArchiveMojo() {
    }

    public ArchiveMojo(String folderName, String outputPath, MavenProject project) {
        this.folderName = folderName;
        this.outputPath = outputPath;
        this.project = project;
    }

    @Parameter(property = "folderName", defaultValue = "allure-results")
    private String folderName;

    @Parameter(property = "outputPath", defaultValue = "${project.build.directory}/archives/allure-results.zip")
    private String outputPath;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            // Найти все папки с заданным именем в проекте
            var baseDir = project != null ? project.getBasedir().toPath() : Path.of(".");
            var archivePath = Paths.get(outputPath);
            Files.createDirectories(archivePath.getParent());

            Path allureResultInArchive = Paths.get("allure-results");

            var atLeastOneFolderFound = new AtomicBoolean(false);
            
            try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(archivePath))) {
                Files.walk(baseDir)
                    .filter(path -> path.toFile().isDirectory())
                    .filter(path -> path.toString().endsWith(folderName))
                    .forEach(path -> {
                        atLeastOneFolderFound.set(true);
                        getLog().info("Found folder to archive: " + path.toAbsolutePath());
                        try {
                            Files.walk(path)
                                .filter(subpath -> !subpath.toFile().isDirectory())
                                .forEach(subpath -> {
                                    ZipEntry zipEntry = new ZipEntry(allureResultInArchive.resolve(path.relativize(subpath)).toString());
                                    try {
                                        zs.putNextEntry(zipEntry);
                                        Files.copy(subpath, zs);
                                        zs.closeEntry();
                                    } catch (IOException e) {
                                        getLog().error("Adding file to archive error", e);
                                    }
                                });
                        } catch (IOException e) {
                            getLog().error("Walking folder to archive error", e);
                        }
                    });
            }

            if (!atLeastOneFolderFound.get())
                throw new MojoExecutionException("None folders found with name: " + folderName);    
            
            getLog().info("Archive created: " + archivePath.toAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Activation error", e);
        }
    }
}

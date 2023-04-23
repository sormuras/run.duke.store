package run.duke.store;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import jdk.tools.Program;
import jdk.tools.Tool;
import jdk.tools.ToolFinder;
import jdk.tools.ToolInstaller;

public record MavenInstaller(String name) implements ToolInstaller {
  public MavenInstaller() {
    this("maven");
  }

  @Override
  public ToolFinder install(Path folder, String version) throws Exception {
    var base = "https://repo.maven.apache.org/maven2/org/apache/maven";
    var mavenWrapperProperties = folder.resolve("maven-wrapper.properties");
    if (Files.notExists(mavenWrapperProperties)) {
      Files.writeString(
          mavenWrapperProperties,
          // language=properties
          """
          distributionUrl=%s/apache-maven/%s/apache-maven-%s-bin.zip
          """
              .formatted(base, version, version));
    }
    var uri = URI.create(base + "/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar#SIZE=62547");
    var mavenWrapperJar = folder.resolve("maven-wrapper.jar");
    if (Files.notExists(mavenWrapperJar)) download(uri, mavenWrapperJar);
    var provider =
        Program.findJavaDevelopmentKitTool(
                "java",
                "-D" + "maven.multiModuleProjectDirectory=.",
                "--class-path=" + mavenWrapperJar,
                "org.apache.maven.wrapper.MavenWrapperMain")
            .orElseThrow();
    return ToolFinder.of(Tool.of(namespace(), name() + '@' + version, provider));
  }

  static void download(URI uri, Path file) throws Exception {
    var parent = file.getParent();
    if (parent != null) Files.createDirectories(parent);
    System.out.printf("<< %s%n", uri);
    try (var stream = uri.toURL().openStream()) {
      var size = Files.copy(stream, file);
      System.out.printf(">> %11d %s%n", size, file.getFileName());
    }
  }
}

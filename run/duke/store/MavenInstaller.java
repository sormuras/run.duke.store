package run.duke.store;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import jdk.tools.Program;
import jdk.tools.Tool;
import jdk.tools.ToolFinder;
import jdk.tools.ToolInstaller;
import run.duke.DukeBrowser;

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
    var browser = DukeBrowser.ofSystem();
    var uri = URI.create(base + "/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar#SIZE=62547");
    var mavenWrapperJar = browser.download(uri, folder.resolve("maven-wrapper.jar"));
    var provider =
        Program.findJavaDevelopmentKitTool(
                "java",
                "-D" + "maven.multiModuleProjectDirectory=.",
                "--class-path=" + mavenWrapperJar,
                "org.apache.maven.wrapper.MavenWrapperMain")
            .orElseThrow();
    return ToolFinder.of(Tool.of(namespace(), name() + '@' + version, provider));
  }
}

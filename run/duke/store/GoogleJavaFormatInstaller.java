package run.duke.store;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import jdk.tools.Program;
import jdk.tools.Tool;
import jdk.tools.ToolInstaller;
import run.duke.DukeBrowser;

public record GoogleJavaFormatInstaller(String name) implements ToolInstaller {
  public GoogleJavaFormatInstaller() {
    this("google-java-format");
  }

  private Map<String, String> assets(String version) {
    var name = "google-java-format";
    var from = "https://github.com/google/" + name;
    if (version.equals("1.15.0")) {
      return Map.of(
          name + "-1.15.0-all-deps.jar",
          from + "/releases/download/v1.15.0/" + name + "-1.15.0-all-deps.jar#SIZE=3519780",
          "README.md",
          from + "/raw/v1.15.0/README.md#SIZE=6270");
    }
    if (version.equals("1.16.0")) {
      return Map.of(
          name + "-1.16.0-all-deps.jar",
          from + "/releases/download/v1.16.0/" + name + "-1.16.0-all-deps.jar#SIZE=3511159",
          "README.md",
          from + "/raw/v1.16.0/README.md#SIZE=6023");
    }
    return Map.of(
        (name + "-%s-all-deps.jar").formatted(version),
        (from + "/releases/download/v%1$s/" + name + "-%1$s-all-deps.jar").formatted(version),
        "README.md",
        (from + "/raw/v%s/README.md").formatted(version));
  }

  @Override
  public Tool install(Path folder, String version) {
    var namespace = namespace();
    var name = name() + '@' + version;
    var browser = DukeBrowser.ofSystem();
    Path jar = null;
    for (var asset : assets(version).entrySet()) {
      var source = URI.create(asset.getValue());
      var target = folder.resolve(asset.getKey());
      browser.download(source, target);
      if (jar == null && target.toString().endsWith(".jar")) jar = target;
    }
    var program = Program.findJavaDevelopmentKitTool("java", "-jar", jar).orElseThrow();
    return Tool.of(namespace, name, program);
  }
}

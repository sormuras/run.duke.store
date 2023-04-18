package run.duke.store;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.spi.ToolProvider;
import jdk.tools.Command;
import jdk.tools.Program;
import jdk.tools.Tool;
import jdk.tools.ToolFinder;
import jdk.tools.ToolInstaller;
import jdk.tools.ToolRunner;

public record EchoInstaller(String name) implements ToolInstaller {
  public EchoInstaller() {
    this("echo");
  }

  @Override
  public ToolFinder install(Path folder, String version) throws Exception {
    var echoJava = folder.resolve("Echo.java");
    if (Files.notExists(echoJava)) {
      // TODO workbench.log("Writing Java source file -> " + folder.toUri());
      Files.writeString(echoJava, ECHO_JAVA);
    }
    var echoJar = folder.resolve("echo.jar");
    if (Files.notExists(echoJar)) {
      // TODO workbench.log("Compiling executable Java archive -> " + folder.toUri());
      var compile = Command.of("javac", "--release", 8, echoJava);
      var archive =
          Command.of("jar", "--create")
              .with("--file", echoJar)
              .with("--main-class", "Echo")
              .with("-C", folder, ".");
      var runner = ToolRunner.ofSystem();
      runner.run(compile);
      runner.run(archive);
    }
    var namespace = getClass().getModule().getName();
    var echoTool = new EchoTool();
    var echoSource = Program.findJavaDevelopmentKitTool("java", echoJava).orElseThrow();
    var echoBinary = Program.findJavaDevelopmentKitTool("java", "-jar", echoJar).orElseThrow();
    return ToolFinder.of(
        Tool.of(namespace, name() + "@" + version, echoTool),
        Tool.of(namespace, name() + ".java@" + version, echoSource),
        Tool.of(namespace, name() + ".jar@" + version, echoBinary),
        Tool.of(namespace, name() + ".tool@" + version, echoTool));
  }

  private static final String ECHO_JAVA =
      // language=java
      """
      class Echo {
        public static void main(String... args) {
          System.out.println(args.length == 0 ? "<silence...>" : String.join(" ", args));
        }
      }
      """;

  record EchoTool(String name) implements Tool, ToolProvider {
    EchoTool() {
      this("echo");
    }

    @Override
    public ToolProvider provider() {
      return this;
    }

    @Override
    public int run(PrintWriter out, PrintWriter err, String... args) {
      out.println(args.length == 0 ? "<silence...>" : String.join(" ", args));
      return 0;
    }
  }
}

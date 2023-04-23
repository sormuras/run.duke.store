/** Defines a selection of commonly-used tool installers and library locators. */
module run.duke.store {
  requires jdk.tools;
  requires run.duke;

  exports run.duke.store;

  provides jdk.tools.ToolInstaller with
      run.duke.store.EchoInstaller,
      run.duke.store.MavenInstaller,
      run.duke.store.GoogleJavaFormatInstaller;
}

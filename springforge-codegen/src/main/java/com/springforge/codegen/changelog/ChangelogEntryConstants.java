
package com.springforge.codegen.changelog;

import java.util.List;

public final class ChangelogEntryConstants {

  private ChangelogEntryConstants() {}

  public static final String CHANGELOG_FILE = "CHANGELOG.md";
  public static final String INPUT_PATH = "changelogs/unreleased";

  public static final List<String> TYPES =
      List.of("Feature", "Change", "Deprecate", "Remove", "Fix", "Security");

  public static final boolean ALLOW_NO_ENTRY = false;
  public static final String DEFAULT_CONTENT = "";
}


package com.springforge.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class TestFileUtils {

  @Test
  public void testGetFile() {

    File file = FileUtils.getFile("file.text");
    assertEquals("file.text", file.getPath());

    file = FileUtils.getFile("my", "dir", "file.text");
    assertEquals("my/dir/file.text".replace("/", File.separator), file.getPath());
  }

  @Test
  public void testDirUtils() {

    File source = new File("src");
    File target = new File("bin/src-copy");

    try {
      FileUtils.copyDirectory(source, target);
    } catch (IOException e) {
      fail();
    }

    assertTrue(target.exists() && target.isDirectory());
    assertNotNull(target.listFiles());
    assertTrue(target.listFiles().length > 0);

    try {
      FileUtils.deleteDirectory(target);
    } catch (IOException e) {
      fail();
    }

    assertFalse(target.exists());
  }

  @Test
  public void testSafeFileName() {
    assertNull(FileUtils.safeFileName(null));
    assertEquals(FileUtils.safeFileName(""), "");

    // trim
    assertEquals("toto.txt", FileUtils.safeFileName(" toto.txt "));

    // accent
    assertEquals("Cesar.txt", FileUtils.safeFileName("César.txt"));

    // illegal
    assertEquals("", FileUtils.safeFileName("?[]/\\=<>:;,'\"&$#*()|~`!{',}%+’«»”“"));

    // space
    assertEquals("a-fil-e.txt", FileUtils.safeFileName("a fil e.txt"));

    // illegal start and end chars
    assertEquals("a-file.txt", FileUtils.safeFileName("-a file.txt"));
    assertEquals("a-file.txt", FileUtils.safeFileName(".a file.txt."));
    assertEquals("hello.txt", FileUtils.safeFileName("hello .txt"));
    assertEquals("hello.txt", FileUtils.safeFileName("hello-_..txt"));
    assertEquals("hello.txt", FileUtils.safeFileName("-hello-_..txt "));

    // legal
    assertEquals("toto.txt", FileUtils.safeFileName("toto.txt"));
    assertEquals("漢字.txt", FileUtils.safeFileName("漢字.txt"));
    assertEquals("hello.txt", FileUtils.safeFileName(" hello.txt"));
  }

  @Test
  public void testStripExtension() {
    assertNull(FileUtils.stripExtension(null));
    assertEquals("", FileUtils.stripExtension(""));
    assertEquals("hello", FileUtils.stripExtension("hello.txt"));
    assertEquals("hello", FileUtils.stripExtension("hello"));
    assertEquals("hel.lo", FileUtils.stripExtension("hel.lo.txt"));
    assertEquals("../filename", FileUtils.stripExtension("../filename.ext"));
  }

  @Test
  public void testGetExtension() {
    assertNull(FileUtils.getExtension(null));
    assertEquals("", FileUtils.getExtension(""));
    assertEquals("txt", FileUtils.getExtension("hello.txt"));
    assertEquals("", FileUtils.getExtension("hello"));
    assertEquals("txt", FileUtils.getExtension("hel.lo.txt"));
    assertEquals("ext", FileUtils.getExtension("../filename.ext"));
  }
}

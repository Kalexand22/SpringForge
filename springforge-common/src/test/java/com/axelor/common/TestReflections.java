
package com.springforge.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.springforge.common.bar.MyBase;
import com.springforge.common.reflections.Reflections;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings("all")
public class TestReflections implements Serializable {

  @Test
  public void testClassFinder() {

    Set<?> all;

    // scan by sub type
    all = Reflections.findSubTypesOf(Map.class).find();

    assertNotNull(all);
    assertTrue(all.size() > 2);

    // scan by annotation within a package
    all = Reflections.findTypes().having(Disabled.class).within("com.springforge.common").find();

    assertNotNull(all);
    assertEquals(2, all.size());

    // scan by annotation within a package
    all = Reflections.findTypes().having(Disabled.class).within("com.springforge.common.foo").find();

    assertNotNull(all);
    assertEquals(1, all.size());

    // scan by sub type and annotation
    all =
        Reflections.findSubTypesOf(MyBase.class).having(Disabled.class).within("com.springforge").find();

    // scan by url pattern
    all = Reflections.findSubTypesOf(Map.class).byURL(".*/springforge-common/.*").find();

    assertNotNull(all);
    assertEquals(4, all.size());
  }

  @Test
  public void testResourceFinder() {
    assertNotNull(Reflections.findResources().byName("(.*)\\.java").find());
  }
}


package com.springforge.codegen.code.entity.model;

import com.springforge.common.StringUtils;
import com.google.common.base.Preconditions;
import java.beans.PropertyDescriptor;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class PropertyAttribute {
  private final String name;
  private final PropertyDescriptor descriptor;

  private final BiConsumer<Object, Object> checkOverride;
  private final Predicate<Object> isAbsent;

  public static PropertyAttribute of(
      String name, PropertyDescriptor descriptor, BiConsumer<Object, Object> checkOverride) {
    return new PropertyAttribute(name, descriptor, checkOverride, Objects::isNull);
  }

  public static PropertyAttribute ofValue(
      String name, PropertyDescriptor descriptor, BiConsumer<Object, Object> checkOverride) {
    return new PropertyAttribute(
        name, descriptor, checkOverride, value -> StringUtils.isBlank(String.valueOf(value)));
  }

  private PropertyAttribute(
      String name,
      PropertyDescriptor descriptor,
      BiConsumer<Object, Object> checkOverride,
      Predicate<Object> isAbsent) {
    Preconditions.checkNotNull(descriptor);
    this.name = StringUtils.notBlank(name) ? name : descriptor.getName();
    this.descriptor = descriptor;
    this.checkOverride = checkOverride != null ? checkOverride : (a, b) -> {};
    this.isAbsent = isAbsent;
  }

  public String getName() {
    return name;
  }

  public PropertyDescriptor getDescriptor() {
    return descriptor;
  }

  public void checkOverride(Object a, Object b) {
    checkOverride.accept(a, b);
  }

  public boolean isAbsent(Object a) {
    return isAbsent.test(a);
  }
}

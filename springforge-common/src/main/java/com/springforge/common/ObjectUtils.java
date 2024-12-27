
package com.springforge.common;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/** This class defines from static helper methods to deal with objects. */
public final class ObjectUtils {

  /**
   * Check whether the given value is an array.
   *
   * @param value the value to check
   * @return true if value is array false otherwise
   */
  public static boolean isArray(Object value) {
    return value != null && value.getClass().isArray();
  }

  /**
   * Check whether the given value is empty.
   *
   * <p>An object value is empty if:
   *
   * <ul>
   *   <li>value is null
   *   <li>value is {@link Optional} and {@link Optional#empty()}
   *   <li>value is {@link Array} with length 0
   *   <li>value is {@link CharSequence} with length 0
   *   <li>value is {@link Collection} or {@link Map} with size 0
   * </ul>
   *
   * @param value the object value to check
   * @return true if empty false otherwise
   */
  public static boolean isEmpty(Object value) {
    if (value == null) {
      return true;
    }
    if (value instanceof Optional) {
      return !((Optional<?>) value).isPresent();
    }
    if (value.getClass().isArray()) {
      return Array.getLength(value) == 0;
    }
    if (value instanceof CharSequence) {
      return ((CharSequence) value).length() == 0;
    }
    if (value instanceof Collection) {
      return ((Collection<?>) value).isEmpty();
    }
    if (value instanceof Map) {
      return ((Map<?, ?>) value).isEmpty();
    }
    return false;
  }

  /**
   * Check whether the given value is not empty.
   *
   * @param value the object value to check
   * @return true if not empty false otherwise
   * @see #isEmpty(Object)
   */
  public static boolean notEmpty(Object value) {
    return !isEmpty(value);
  }
}

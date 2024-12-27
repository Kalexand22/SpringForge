
package com.springforge.codegen.code;

/** Interface for all code elements that can emit some code. */
public interface JavaElement {

  /**
   * Emit the code.
   *
   * @param writer the pojo writer
   */
  void emit(JavaWriter writer);
}

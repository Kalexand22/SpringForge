
package com.springforge.codegen.code;

import java.lang.reflect.Modifier;

/** This class can be used to define java method params. */
public class JavaParam extends JavaAnnotable<JavaParam> {

  /**
   * Create a new instance of {@link JavaParam}.
   *
   * @param name the parameter name
   * @param type the parameter type
   */
  public JavaParam(String name, String type) {
    super(name, type);
  }

  /**
   * Create a new instance of {@link JavaParam}.
   *
   * @param name the parameter name
   * @param type the parameter type
   * @param isFinal whether the param is final
   */
  public JavaParam(String name, String type, boolean isFinal) {
    super(name, type, Modifier.FINAL);
  }
}

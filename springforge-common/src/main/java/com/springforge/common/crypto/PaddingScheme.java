
package com.springforge.common.crypto;

import javax.crypto.Cipher;

/** {@link Cipher} padding schemes. */
public enum PaddingScheme {
  NONE("NoPadding"),

  PKCS5("PKCS5Padding");

  private String name;

  private PaddingScheme(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}

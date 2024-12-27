
package com.springforge.common.crypto;

/**
 * The unchecked exception is thrown by {@link Encryptor} classes if there is any error while
 * encrypting or decrypting values.
 */
public class EncryptorException extends RuntimeException {

  private static final long serialVersionUID = 7340536599422956645L;

  public EncryptorException(String message, Throwable cause) {
    super(message, cause);
  }

  public EncryptorException(String message) {
    super(message);
  }

  public EncryptorException(Throwable cause) {
    super(cause);
  }
}

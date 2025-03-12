package github.lianyutian.cshop.common.exception;

import java.util.List;

/**
 * @author lianyutian
 * @since 2025/3/6
 * @version 1.0
 */
public class CompositeException extends RuntimeException {
  private final List<String> errors;

  public CompositeException(List<String> errors) {
    super(String.join("; ", errors));
    this.errors = errors;
  }
}

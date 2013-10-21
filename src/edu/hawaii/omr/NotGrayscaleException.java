package edu.hawaii.omr;

/**
 * Indicates that the image is not a grayscale image.
 * @author Christopher Foo
 *
 */
public class NotGrayscaleException extends Exception {

  /**
   * Generated serial version ID.
   */
  private static final long serialVersionUID = 3588291094357657167L;
  
  /**
   * Creates a new {@link NotGrayscaleException} with a default message.
   */
  public NotGrayscaleException() {
    super("Error: The image is not a grayscale image.");
  }

}

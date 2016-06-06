package uncmn.downlink;

/**
 * An encapsulation for status of a download.
 */
public final class DownloadStatus {

  public static final int STATUS_COMPLETED = 1;
  public static final int STATUS_FAILED = 2;
  public static final int STATUS_DOWNLOAD_IN_PROGRESS = 3;
  public static final int STATUS_NOT_AVAILABLE = 4;

  private int status;
  private String message;

  /**
   * Default constructor.
   *
   * @param status download status.
   */
  DownloadStatus(int status) {
    this.status = status;
  }

  DownloadStatus(int status, String message) {
    this.status = status;
    this.message = message;
  }

  /**
   * @return whether status is completed.
   */
  public boolean isCompleted() {
    return status == STATUS_COMPLETED;
  }

  /**
   * @return whether status is failed.
   */
  public boolean isFailed() {
    return status == STATUS_FAILED;
  }

  /**
   * @return Whether download is available.
   */
  public boolean isNotAvailable() {
    return status == STATUS_NOT_AVAILABLE;
  }

  public static DownloadStatus notAvailable() {
    return new DownloadStatus(STATUS_NOT_AVAILABLE);
  }

  public String message() {
    return message;
  }
}

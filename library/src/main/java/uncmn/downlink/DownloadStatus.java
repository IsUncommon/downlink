package uncmn.downlink;

/**
 * An encapsulation for status of a download.
 */
public final class DownloadStatus {

  public static final int STATUS_COMPLETED = 1;
  public static final int STATUS_FAILED = 2;
  public static final int STATUS_DOWNLOAD_IN_PROGRESS = 3;
  public static final int STATUS_NOT_AVAILABLE = 4;
  public static final int STATUS_QUEUED = 5;
  public static final int STATUS_CANCELED = 6;

  private int status;
  private String message;
  private String url;
  private String contentType;
  private String metaData;

  /**
   * Default constructor.
   *
   * @param status download status.
   */
  DownloadStatus(int status) {
    this.status = status;
  }

  DownloadStatus(int status, String message, String url, String contentType, String metaData) {
    this.status = status;
    this.message = message;
    this.url = url;
    this.contentType = contentType;
    this.metaData = metaData;
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
   * @return whether status is download in progress.
   */
  public boolean isDownloadInProgress() {
    return status == STATUS_DOWNLOAD_IN_PROGRESS;
  }

  /**
   * @return Whether download is available.
   */
  public boolean isNotAvailable() {
    return status == STATUS_NOT_AVAILABLE;
  }

  /**
   * @return Whether download is queued.
   */
  public boolean isQueued() {
    return status == STATUS_QUEUED;
  }

  public boolean isCanceled() {
    return status == STATUS_CANCELED;
  }

  public static DownloadStatus notAvailable() {
    return new DownloadStatus(STATUS_NOT_AVAILABLE);
  }

  public static DownloadStatus queued() {
    return new DownloadStatus(STATUS_QUEUED);
  }

  public static DownloadStatus completed() {
    return new DownloadStatus(STATUS_COMPLETED);
  }

  public static DownloadStatus failed() {
    return new DownloadStatus(STATUS_FAILED);
  }

  public static DownloadStatus canceled() {
    return new DownloadStatus(STATUS_CANCELED);
  }

  public static DownloadStatus inProgress() {
    return new DownloadStatus(STATUS_DOWNLOAD_IN_PROGRESS);
  }

  public String message() {
    return message;
  }

  public String contentType() {
    return contentType;
  }

  public String url() {
    return url;
  }

  public String metaData() {
    return metaData;
  }

  @Override public String toString() {
    return "DownloadStatus{"
        +
        "status="
        + status
        + ", message='"
        + message
        + '\''
        + ", url='"
        + url
        + '\''
        + ", contentType='"
        + contentType
        + '\''
        + ", metaData='"
        + metaData
        + '\''
        +
        '}';
  }
}

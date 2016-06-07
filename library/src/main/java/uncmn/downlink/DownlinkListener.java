package uncmn.downlink;

/**
 * A downlink listener to listen to download progress.
 */
public interface DownlinkListener {
  /**
   * Notified on download complete.
   *
   * @param url Url value.
   */
  void onCompleted(String url);

  /**
   * Notified when a url is queued for download.
   * @param url Url value.
   */
  void onQueued(String url);

  /**
   * Notified when a download fails.
   *
   * @param url Url value.
   * @param t throwable indicating failure.
   */
  void onFailure(String url, Throwable t);

  /**
   * Notified when a download progresses.
   *
   * @param url Url value.
   * @param downloadSize Downloaded size in bytes.
   * @param totalSize Total size in bytes.
   */
  void onProgress(String url, long downloadSize, long totalSize);

  /**
   * Notified when download is canceled
   *
   * @param url Url value.
   */
  void onCanceled(String url);
}

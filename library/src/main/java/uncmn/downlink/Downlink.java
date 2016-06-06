package uncmn.downlink;

import android.util.Log;
import com.jakewharton.disklrucache.DiskLruCache;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

/**
 * Download manager.
 */
public final class Downlink {
  private static final int DEFAULT_CACHE_SIZE_IN_BYTES = 50 * 1024 * 1024;
  private Logger logger = new Logger() {
    @Override public void log(String message) {
      //do nothing
    }
  };
  private final File rootDir;
  private FileStore fileStore;
  private long cacheSizeInBytes = DEFAULT_CACHE_SIZE_IN_BYTES;
  private ConcurrentHashMap<String, DownloadWorker> workerMap = new ConcurrentHashMap<>();
  private ExecutorService executorService = Executors.newSingleThreadExecutor();
  private OkHttpClient httpClient;
  private HashMap<String, List<DownlinkListener>> listenerMap = new HashMap<>();
  private DownlinkListener internalDownlinkListener = new DownlinkListener() {
    @Override public void onCompleted(String url) {
      logger.log("Url downloaded -- " + url);
      if (listenerMap.containsKey(url)) {
        for (DownlinkListener listener : listenerMap.get(url)) {
          listener.onCompleted(url);
        }
      }
    }

    @Override public void onQueued(String url) {
      logger.log("Url queued -- " + url);
      if (listenerMap.containsKey(url)) {
        for (DownlinkListener listener : listenerMap.get(url)) {
          listener.onQueued(url);
        }
      }
    }

    @Override public void onFailure(String url, Throwable t) {
      logger.log("Url download failed -- " + url + " ---" + Log.getStackTraceString(t));
      if (listenerMap.containsKey(url)) {
        for (DownlinkListener listener : listenerMap.get(url)) {
          listener.onFailure(url, t);
        }
      }
    }

    @Override public void onProgress(String url, long downloadSize, long totalSize) {
      logger.log(
          String.format("Url - %s, Total - %d%n, Download -- %d%n", url, totalSize, downloadSize));
      if (listenerMap.containsKey(url)) {
        for (DownlinkListener listener : listenerMap.get(url)) {
          listener.onProgress(url, downloadSize, totalSize);
        }
      }
    }

    @Override public void onCanceled(String url) {
      logger.log("Url canceled -- " + url);
      if (listenerMap.containsKey(url)) {
        for (DownlinkListener listener : listenerMap.get(url)) {
          listener.onCanceled(url);
        }
      }
    }
  };

  /**
   * @param rootDir Root directory at which cache entries should be saved.
   */
  private Downlink(String rootDir) {
    this.rootDir = new File(rootDir);
    init();
  }

  private void init() {
    if (!rootDir.exists()) {
      boolean created = rootDir.mkdirs();
      if (!created) {
        throw new RuntimeException(
            "Failed creating directory at path -- " + rootDir.getAbsolutePath());
      }
    }
    httpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build();
    this.fileStore = new FileStore(rootDir, cacheSizeInBytes);
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
    this.fileStore.setLogger(logger);
  }

  /**
   * @param rootDir Directory to which downloads will happen.
   * @return Downlink instance.
   */
  public static Downlink create(String rootDir) {
    Downlink downlink = new Downlink(rootDir);
    return downlink;
  }

  /**
   * @param url Url value.
   * @return Download Status object representing the download state of the url.
   */
  public DownloadStatus downloadStatus(String url) {
    String key = Util.hashKey(url);
    DiskLruCache.Snapshot snapshot = null;
    try {
      snapshot = fileStore.getDiskLruCache().get(key);
      if (snapshot != null) {
        BufferedSource source =
            Okio.buffer(Okio.source(snapshot.getInputStream(FileStore.STATUS_INDEX)));
        int status = source.readInt();
        source.close();
        DownloadStatus downloadStatus = new DownloadStatus(status);
        if (downloadStatus.isFailed()) {
          source = Okio.buffer(Okio.source(snapshot.getInputStream(FileStore.FILE_INDEX)));
          String message = source.readUtf8LineStrict();
          source.close();
          downloadStatus = new DownloadStatus(status, message);
        }
        snapshot.close();
        return downloadStatus;
      }
    } catch (IOException e) {
      logger.log("Exception occurred -- " + Log.getStackTraceString(e));
    } finally {
      if (snapshot != null) {
        snapshot.close();
      }
    }
    return DownloadStatus.notAvailable();
  }

  /**
   * Cancel a download for given url.
   *
   * @param url Download to be canceled.
   */
  public void cancelDownload(String url) {
    final String key = Util.hashKey(url);
    if (workerMap.containsKey(key)) {
      DownloadWorker runner = workerMap.remove(key);
      runner.cancelDownload();
    } else {
      logger.log("Nothing to cancel");
    }
  }

  /**
   * Queue a download if one does not already exists.
   *
   * @param url Download url.
   */
  public void queue(String url) {
    final String key = Util.hashKey(url);
    if (!workerMap.containsKey(key)) {
      final DownloadWorker worker =
          new DownloadWorker(key, httpClient, url, fileStore, internalDownlinkListener);
      workerMap.put(key, worker);
      executorService.submit(new Runnable() {
        @Override public void run() {
          try {
            worker.download();
          } catch (Exception e) {
            logger.log("Exception occurred -- " + Log.getStackTraceString(e));
          }
          workerMap.remove(key);
        }
      });
      internalDownlinkListener.onQueued(url);
    } else {
      logger.log("Already contains an executorService for url -- " + url);
    }
  }

  /**
   * Add listener.
   *
   * @param url Url interested in.
   * @param downlinkListener Downlink listener.
   */
  public void addListener(String url, DownlinkListener downlinkListener) {
    if (!listenerMap.containsKey(url)) {
      listenerMap.put(url, new ArrayList<DownlinkListener>());
    }
    List<DownlinkListener> list = listenerMap.get(url);
    if (!list.contains(downlinkListener)) {
      list.add(downlinkListener);
    }
  }

  /**
   * Remove listener.
   *
   * @param url url interested in.
   * @param downlinkListener Downlink listener.
   */
  public void removeListener(String url, DownlinkListener downlinkListener) {
    if (listenerMap.containsKey(url)) {
      listenerMap.get(url).remove(downlinkListener);
    }
  }

  /**
   * A download worker class.
   */
  public static class DownloadWorker {

    private final String downloadUrl;
    private final FileStore fileStore;
    private final OkHttpClient client;
    private final DownlinkListener listener;
    private final String fileKey;
    private volatile boolean cancelDownload = false;
    private Call call;

    public DownloadWorker(String fileKey, OkHttpClient client, String url, FileStore fileStore,
        DownlinkListener listener) {
      //add download in progress
      this.downloadUrl = url;
      this.client = client;
      this.fileStore = fileStore;
      this.listener = listener;
      this.fileKey = fileKey;
    }

    public void cancelDownload() {
      if (!cancelDownload) {
        this.cancelDownload = true;
        if (call != null && !call.isCanceled()) {
          call.cancel();
          listener.onCanceled(downloadUrl);
        }
      }
    }

    public void download() {
      Request request = new Request.Builder().url(downloadUrl).build();
      DiskLruCache lruCache = fileStore.getDiskLruCache();
      Exception failedException = null;
      try {
        DiskLruCache.Editor editor = lruCache.edit(fileKey);
        addStatus(editor);
        call = client.newCall(request);
        Response response = call.execute();
        if (!response.isSuccessful()) {
          String message = response.message();
          failedException = new Exception(message);
        } else {
          //lets add content type
          editor = lruCache.edit(fileKey);
          BufferedSink bufferedcontentTypeSink =
              Okio.buffer(Okio.sink(editor.newOutputStream(FileStore.CONTENT_TYPE_INDEX)));
          bufferedcontentTypeSink.writeUtf8(response.body().contentType().type());
          bufferedcontentTypeSink.flush();
          bufferedcontentTypeSink.close();
          editor.commit();
          //---

          editor = lruCache.edit(fileKey);
          //lets add the file
          final long totalSize = response.body().contentLength();
          BufferedSource bufferedFileSource =
              Okio.buffer(new ForwardingSource(Okio.source(response.body().byteStream())) {
                long totalBytesRead = 0L;

                @Override public long read(Buffer sink, long byteCount) throws IOException {
                  long bytesRead = super.read(sink, byteCount);
                  // read() returns the number of bytes read, or -1 if this source is exhausted.
                  totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                  listener.onProgress(downloadUrl, totalBytesRead, totalSize);
                  return bytesRead;
                }
              });
          BufferedSink bufferedFileSink =
              Okio.buffer(Okio.sink(editor.newOutputStream(FileStore.FILE_INDEX)));
          bufferedFileSink.writeAll(bufferedFileSource);
          bufferedFileSource.close();
          editor.commit();
        }
      } catch (IOException e) {
        failedException = new Exception(Log.getStackTraceString(e));
      }
      if (failedException != null) {
        try {
          DiskLruCache.Editor editor = lruCache.edit(fileKey);
          //set failed state in the entry
          BufferedSink bufferedStatusSink =
              Okio.buffer(Okio.sink(editor.newOutputStream(FileStore.STATUS_INDEX)));
          bufferedStatusSink.writeInt(DownloadStatus.STATUS_FAILED);
          bufferedStatusSink.flush();
          bufferedStatusSink.close();
          editor.commit();

          editor = lruCache.edit(fileKey);
          BufferedSink bufferedFileSink =
              Okio.buffer(Okio.sink(editor.newOutputStream(FileStore.FILE_INDEX)));
          bufferedFileSink.writeUtf8(failedException.getMessage());
          bufferedFileSink.close();
          editor.commit();
          listener.onFailure(downloadUrl, failedException);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        DiskLruCache.Editor editor = null;
        try {
          editor = lruCache.edit(fileKey);
          //set failed state in the entry
          BufferedSink bufferedStatusSink =
              Okio.buffer(Okio.sink(editor.newOutputStream(FileStore.STATUS_INDEX)));
          bufferedStatusSink.writeInt(DownloadStatus.STATUS_COMPLETED);
          bufferedStatusSink.flush();
          bufferedStatusSink.close();
          editor.commit();
          listener.onCompleted(downloadUrl);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    private void addStatus(DiskLruCache.Editor editor) throws IOException {
      //lets add status first
      BufferedSink bufferedStatusSink =
          Okio.buffer(Okio.sink(editor.newOutputStream(FileStore.STATUS_INDEX)));
      bufferedStatusSink.writeInt(DownloadStatus.STATUS_DOWNLOAD_IN_PROGRESS);
      bufferedStatusSink.flush();
      bufferedStatusSink.close();

      bufferedStatusSink =
          Okio.buffer(Okio.sink(editor.newOutputStream(FileStore.CONTENT_TYPE_INDEX)));
      bufferedStatusSink.writeUtf8("");
      bufferedStatusSink.flush();
      bufferedStatusSink.close();

      bufferedStatusSink = Okio.buffer(Okio.sink(editor.newOutputStream(FileStore.FILE_INDEX)));
      bufferedStatusSink.writeUtf8("");
      bufferedStatusSink.flush();
      bufferedStatusSink.close();

      bufferedStatusSink = Okio.buffer(Okio.sink(editor.newOutputStream(FileStore.META_INDEX)));
      bufferedStatusSink.writeUtf8("");
      bufferedStatusSink.flush();
      bufferedStatusSink.close();

      editor.commit();
      //---
    }
  }
}

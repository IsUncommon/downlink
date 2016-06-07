package uncmn.downlink;

import android.util.Log;
import com.jakewharton.disklrucache.DiskLruCache;
import java.io.File;
import java.io.IOException;

/**
 * File storage class.
 */
public final class FileStore {
  private static final int CACHE_VERSION = 1;

  private static final int DISK_CACHE_ENTRIES = 5;

  public static final int STATUS_INDEX = 0;
  public static final int CONTENT_TYPE_INDEX = 1;
  public static final int FILE_INDEX = 2;
  public static final int URL_INDEX = 3;
  public static final int META_INDEX = 4;

  private final File cacheRootDir;
  private final long cacheSizeInBytes;

  private Logger logger = new Logger() {
    @Override public void log(String message) {
      //do nothing
    }
  };
  private DiskLruCache diskLruCache;

  /**
   * Default constructor.
   *
   * @param rootDir Root directory.
   * @param sizeInBytes Size of the file store.
   */
  public FileStore(File rootDir, long sizeInBytes) {
    this.cacheSizeInBytes = sizeInBytes;
    this.cacheRootDir = rootDir;
    init();
  }

  private void init() {
    try {
      this.diskLruCache =
          DiskLruCache.open(cacheRootDir, CACHE_VERSION, DISK_CACHE_ENTRIES, cacheSizeInBytes);
    } catch (IOException e) {
      logger.log("Exception occurred -- " + Log.getStackTraceString(e));
    }
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public DiskLruCache getDiskLruCache() {
    return diskLruCache;
  }
}

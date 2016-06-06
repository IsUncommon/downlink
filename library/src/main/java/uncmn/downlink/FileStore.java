package uncmn.downlink;

import android.util.Log;
import com.jakewharton.disklrucache.DiskLruCache;
import java.io.File;
import java.io.IOException;

/**
 * File storage class.
 */
public final class FileStore {
  //50 MB default size
  private static final int DEFAULT_CACHE_SIZE_IN_BYTES = 50 * 1024 * 1024;
  private static final int CACHE_VERSION = 1;

  private static final int DISK_CACHE_ENTRIES = 4;
  public static final int STATUS_INDEX = 0;
  public static final int CONTENT_TYPE_INDEX = 1;
  public static final int FILE_INDEX = 2;
  public static final int META_INDEX = 3;

  private final File cacheRootDir;
  private final long cacheSizeInBytes;

  private Logger logger = new Logger() {
    @Override public void log(String message) {
      //do nothing
    }
  };
  private DiskLruCache diskLruCache;

  public FileStore(File cacheRootDir, long cacheSizeInBytes) {
    this.cacheSizeInBytes = cacheSizeInBytes;
    this.cacheRootDir = cacheRootDir;
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
package uncmn.downlink.sample;

import android.app.Application;
import java.io.File;
import timber.log.Timber;
import uncmn.downlink.Downlink;
import uncmn.downlink.Logger;

/**
 * Main sample application.
 */
public class MainApplication extends Application {
  private static final int DEFAULT_CACHE_MAX_SIZE_IN_BYTES = 250 * 1024 * 1024;

  private Downlink downlink;

  @Override public void onCreate() {
    super.onCreate();
    Timber.plant(new TimberThreadDebugTree());
    File downloadDir = new File(getFilesDir(), "downloads");
    downlink = Downlink.create(downloadDir.getAbsolutePath(), DEFAULT_CACHE_MAX_SIZE_IN_BYTES);
    downlink.setLogger(new Logger() {
      @Override public void log(String message) {
        Timber.d(message);
      }
    });
  }

  public Downlink getDownlink() {
    return downlink;
  }
}

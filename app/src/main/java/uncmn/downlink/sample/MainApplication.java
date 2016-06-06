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
  private Downlink downlink;

  @Override public void onCreate() {
    super.onCreate();
    Timber.plant(new TimberThreadDebugTree());
    File downloadDir = new File(getFilesDir(), "downloads");
    downlink = Downlink.create(downloadDir.getAbsolutePath());
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

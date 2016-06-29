package uncmn.downlink.sample;

import android.app.Application;
import java.io.File;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
      @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Timber.d("Intercepting request url -- %s", request.url());
        return chain.proceed(request);
      }
    }).build();
    downlink =
        Downlink.create(downloadDir.getAbsolutePath(), DEFAULT_CACHE_MAX_SIZE_IN_BYTES, client);
    downlink.setLogger(new Logger() {
      @Override public void log(String message) {
        Timber.d(message);
      }
    });
  }

  public Downlink downlink() {
    return downlink;
  }
}

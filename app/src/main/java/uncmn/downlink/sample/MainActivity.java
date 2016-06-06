package uncmn.downlink.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import timber.log.Timber;
import uncmn.downlink.Downlink;
import uncmn.downlink.DownloadStatus;
import uncmn.downlink.Logger;

public class MainActivity extends AppCompatActivity {

  String pdfUrl = "https://services.github.com/kit/downloads/github-git-cheat-sheet.pdf";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    MainApplication application = (MainApplication) getApplication();
    Downlink downlink = application.getDownlink();
    downlink.setLogger(new Logger() {
      @Override public void log(String message) {
        Timber.d(message);
      }
    });
    DownloadStatus status = downlink.downloadStatus(pdfUrl);
    if (status.isNotAvailable()) {
      Timber.d("Queueing download");
      downlink.queue(pdfUrl);
    } else if (status.isCompleted()) {
      Timber.d("Download completed");
    } else if (status.isFailed()) {
      Timber.d("Download status failed  -- \n %s", status.message());
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_settings, menu);
    return super.onCreateOptionsMenu(menu);
  }
}

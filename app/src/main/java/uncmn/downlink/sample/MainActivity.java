package uncmn.downlink.sample;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;
import uncmn.downlink.Downlink;
import uncmn.downlink.DownloadStatus;
import uncmn.downlink.Logger;

public class MainActivity extends AppCompatActivity {
  private static final String[] URLS = new String[] {
      "https://dl.dropboxusercontent.com/u/62988391/downlink/7789f6f3985d5ab67bf32bd609ec7daa.png",
      "https://dl.dropboxusercontent.com/u/62988391/downlink/github-git-cheat-sheet.pdf",
      "https://dl.dropboxusercontent.com/u/62988391/downlink/muzei.mp4",
      "https://dl.dropboxusercontent.com/u/62988391/downlink/maven-metadata.xml",
      "https://dl.dropboxusercontent.com/u/62988391/downlink/join.txt",
      "https://services.github.com/kit/downloads/github-git-cheat-sheet.pdf"
  };

  @BindView(R.id.recycler) RecyclerView recyclerView;
  private Unbinder binder;
  private DownloadsAdapter downloadsAdapter;
  private Subscription subscription;
  private PublishSubject<String> subject;
  private Downlink downlink;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    binder = ButterKnife.bind(this);
    MainApplication application = (MainApplication) getApplication();
    downlink = application.downlink();
    downlink.setLogger(new Logger() {
      @Override public void log(String message) {
        Timber.d(message);
      }
    });
    downloadsAdapter = new DownloadsAdapter(downlink);
    recyclerView.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    recyclerView.setAdapter(downloadsAdapter);
    recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
          RecyclerView.State state) {
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
            getResources().getDisplayMetrics());
        outRect.set(padding, padding, padding, padding);
      }
    });
    setupSubscriptions();
  }

  private void setupSubscriptions() {
    subject = PublishSubject.create();
    subscription = subject.observeOn(Schedulers.io())
        .flatMap(new Func1<String, Observable<List<DownloadableItem>>>() {
          @Override public Observable<List<DownloadableItem>> call(String s) {
            Timber.d("Constructing downloadables....");
            List<DownloadableItem> list = new ArrayList<>();
            for (int i = 0; i < URLS.length; i++) {
              DownloadStatus status = downlink.downloadStatus(URLS[i]);
              list.add(DownloadableItem.builder()
                  .name("Item " + (i + 1))
                  .url(URLS[i])
                  .status(status)
                  .build());
            }
            return Observable.just(list);
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<List<DownloadableItem>>() {
          @Override public void onCompleted() {

          }

          @Override public void onError(Throwable e) {
            Timber.d(e, "Error occurred");
          }

          @Override public void onNext(List<DownloadableItem> downloadableItems) {
            Timber.d("Setting data in adapter...");
            downloadsAdapter.setData(downloadableItems);
          }
        });
  }

  @Override protected void onResume() {
    Timber.d("On Resumed...");
    super.onResume();
    subject.onNext("");
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_settings, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
      startSettings();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void startSettings() {
    Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);
  }

  @Override protected void onDestroy() {
    binder.unbind();
    downloadsAdapter.handleOnDestroy();
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
    super.onDestroy();
  }
}

package uncmn.downlink.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Settings.
 */
public class SettingsActivity extends AppCompatActivity {

  private Unbinder unbinder;
  @BindView(R.id.size) TextView size;
  @BindView(R.id.max_size) TextView maxSize;
  @BindView(R.id.maxsize_panel) LinearLayout maxSizePanel;
  @BindView(R.id.clear_downlink_panel) LinearLayout clearPanel;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.settings);
    final MainApplication application = (MainApplication) getApplication();
    unbinder = ButterKnife.bind(this);
    setSizes();
    maxSizePanel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        //show size options.
      }
    });
    clearPanel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        application.downlink().clear();
        finish();
      }
    });
  }

  private void setSizes() {
    final MainApplication application = (MainApplication) getApplication();
    size.setText(application.downlink().size() + " bytes");
    maxSize.setText(application.downlink().maxSize() + " bytes");
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override protected void onDestroy() {
    unbinder.unbind();
    super.onDestroy();
  }
}

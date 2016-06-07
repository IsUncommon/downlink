package uncmn.downlink.sample;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;
import uncmn.commons.StringUtils;
import uncmn.downlink.Downlink;
import uncmn.downlink.DownlinkListener;
import uncmn.downlink.DownloadStatus;

/**
 * Downloads Adapter.
 */
public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadsViewHolder>
    implements DownlinkListener {

  private final Downlink downlink;
  private final List<DownloadableItem> items = new ArrayList<>();
  private List<WeakReference<DownloadsViewHolder>> holders = new ArrayList<>();
  private Handler handler = new Handler();

  public DownloadsAdapter(Downlink downlink) {
    this.downlink = downlink;
  }

  /**
   * Set new data.
   *
   * @param items data to be set.
   */
  public void setData(List<DownloadableItem> items) {
    this.items.clear();
    this.items.addAll(items);
    for (DownloadableItem item : items) {
      downlink.addListener(item.url(), this);
    }
    notifyDataSetChanged();
  }

  @Override public DownloadsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return DownloadsViewHolder.create(parent, downlink);
  }

  @Override
  public void onBindViewHolder(DownloadsViewHolder holder, int position, List<Object> payloads) {
    //handle payloads
    super.onBindViewHolder(holder, position, payloads);
  }

  @Override public void onBindViewHolder(DownloadsViewHolder holder, int position) {
    holder.bind(items.get(position));
  }

  @Override public int getItemCount() {
    return items.size();
  }

  @Override public void onCompleted(final String url) {
    handler.post(new Runnable() {
      @Override public void run() {
        Timber.d("Download completed -- %s", url);
        int position = getItemPositionFromUrl(url);
        DownloadableItem item = items.get(position);
        items.set(position, item.toBuilder().status(DownloadStatus.completed()).build());
        notifyItemChanged(position);
      }
    });
  }

  @Override public void onQueued(final String url) {
    handler.post(new Runnable() {
      @Override public void run() {
        Timber.d("Download queued -- %s", url);
        int position = getItemPositionFromUrl(url);
        DownloadableItem item = items.get(position);
        items.set(position, item.toBuilder().status(DownloadStatus.queued()).build());
        notifyItemChanged(position);
      }
    });
  }

  @Override public void onFailure(final String url, final Throwable t) {
    handler.post(new Runnable() {
      @Override public void run() {
        Timber.d("Download failed -- %s", url);
        int position = getItemPositionFromUrl(url);
        DownloadableItem item = items.get(position);
        items.set(position, item.toBuilder().status(DownloadStatus.failed()).build());
        notifyItemChanged(position);
      }
    });
  }

  @Override
  public void onProgress(final String url, final long downloadSize, final long totalSize) {
    handler.post(new Runnable() {
      @Override public void run() {
        Timber.d("Download progress -- %s %d %d", url, downloadSize, totalSize);
        int position = getItemPositionFromUrl(url);
        DownloadableItem item = items.get(position);
        items.set(position, item.toBuilder()
            .status(DownloadStatus.inProgress())
            .size(downloadSize)
            .totalSize(totalSize)
            .build());
        notifyItemChanged(position);
      }
    });
  }

  @Override public void onCanceled(final String url) {
    handler.post(new Runnable() {
      @Override public void run() {
        Timber.d("Download canceled -- %s", url);
        int position = getItemPositionFromUrl(url);
        DownloadableItem item = items.get(position);
        items.set(position, item.toBuilder().status(DownloadStatus.canceled()).build());
        notifyItemChanged(position);
      }
    });
  }

  private int getItemPositionFromUrl(String url) {
    for (int i = 0; i < items.size(); i++) {
      if (StringUtils.equals(items.get(i).url(), url)) {
        return i;
      }
    }
    return 0;
  }

  /**
   * A view holder class.
   */
  public static final class DownloadsViewHolder extends RecyclerView.ViewHolder {

    private final Unbinder binder;
    private final Downlink downlink;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.url) TextView url;
    @BindView(R.id.download_status) TextView downloadStatus;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    /**
     * Default constructor.
     *
     * @param itemView Item view.
     * @param downlink Downlink instance.
     */
    public DownloadsViewHolder(View itemView, Downlink downlink) {
      super(itemView);
      this.downlink = downlink;
      binder = ButterKnife.bind(this, itemView);
    }

    /**
     * Create a view holder.
     *
     * @param parent Parent view.
     * @param downlink Downlink instance.
     * @return New view holder.
     */
    public static DownloadsViewHolder create(ViewGroup parent, Downlink downlink) {
      View itemView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_downloadable, parent, false);
      return new DownloadsViewHolder(itemView, downlink);
    }

    /**
     * Unbind and release resources.
     */
    public void unbind() {
      binder.unbind();
      itemView.setOnClickListener(null);
    }

    /**
     * Bind downloadable item.
     *
     * @param item Download item.
     */
    public void bind(DownloadableItem item) {
      itemView.setOnClickListener(null);
      name.setText(item.name());
      url.setText(item.url());
      progressBar.setVisibility(View.GONE);
      progressBar.setMax(100);
      downloadStatus.setVisibility(View.VISIBLE);
      if (item.status().isNotAvailable()) {
        downloadStatus.setText(R.string.status_not_available);
        downloadStatus.setVisibility(View.GONE);
        setupClickAsDownload(item);
      } else if (item.status().isCompleted()) {
        downloadStatus.setText(R.string.download_completed);
        setupClickAsOpen();
      } else if (item.status().isFailed()) {
        downloadStatus.setText(R.string.download_failed);
        setupClickAsRedownload();
      } else if (item.status().isQueued()) {
        downloadStatus.setText(R.string.download_queued);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
      } else if (item.status().isCanceled()) {
        downloadStatus.setText(R.string.download_canceled);
        setupClickAsRedownload();
      } else if (item.status().isDownloadInProgress()) {
        downloadStatus.setText(R.string.download_progress);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(false);
        int progress = (int) Math.round((double) item.size() * 100 / (double) item.totalSize());
        progressBar.setProgress(progress);
        setupClickAsCancel();
      }
    }

    private void setupClickAsCancel() {

    }

    private void setupClickAsRedownload() {

    }

    private void setupClickAsOpen() {

    }

    private void setupClickAsDownload(final DownloadableItem item) {
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          new AlertDialog.Builder(itemView.getContext()).setTitle(
              itemView.getContext().getString(R.string.download_suffix) + item.name())
              .setMessage(itemView.getContext().getString(R.string.download_message))
              .setPositiveButton(itemView.getContext().getString(R.string.yes),
                  new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                      Timber.d("Download item now..");
                      itemView.setOnClickListener(null);
                      downlink.queue(item.url());
                    }
                  })
              .setNegativeButton(itemView.getContext().getString(R.string.no),
                  new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                      Timber.d("Not needed to download item");
                    }
                  })
              .show();
        }
      });
    }
  }

  /**
   * Handle destroy. Releases all resources.
   */
  public void handleOnDestroy() {
    for (WeakReference<DownloadsViewHolder> holder : holders) {
      if (holder.get() != null) {
        holder.get().unbind();
      }
    }
    for (DownloadableItem item : items) {
      downlink.removeListener(item.url(), this);
    }
    handler.removeCallbacksAndMessages(null);
  }
}

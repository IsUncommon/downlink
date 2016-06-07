package uncmn.downlink.sample;

import com.google.auto.value.AutoValue;
import uncmn.downlink.DownloadStatus;

/**
 * Downloadable item encapsulation.
 */
@AutoValue public abstract class DownloadableItem {
  public abstract String url();

  public abstract String name();

  public abstract DownloadStatus status();

  public abstract long totalSize();

  public abstract long size();

  public static Builder builder() {
    return new AutoValue_DownloadableItem.Builder().size(1).totalSize(1);
  }

  public Builder toBuilder() {
    return new AutoValue_DownloadableItem.Builder(this);
  }

  @AutoValue.Builder public abstract static class Builder {
    public abstract DownloadableItem build();

    public abstract Builder url(String url);

    public abstract Builder name(String name);

    public abstract Builder status(DownloadStatus status);

    public abstract Builder totalSize(long totalSize);

    public abstract Builder size(long size);
  }
}

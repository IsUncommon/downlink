package uncmn.downlink.sample;

import timber.log.Timber;
import uncmn.commons.StringUtils;

/**
 * Timber base logtree that gives thread info.
 */
public class TimberThreadDebugTree extends Timber.DebugTree {
  @Override protected void log(int priority, String tag, String message, Throwable t) {
    String currentThread = Thread.currentThread().getName();
    if (StringUtils.isEmpty(currentThread)) {
      super.log(priority, tag, message, t);
    } else {
      super.log(priority, tag,
          new StringBuffer(currentThread).append("/").append(message).toString(), t);
    }
  }
}

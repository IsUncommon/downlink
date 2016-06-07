package uncmn.downlink;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilities.
 */
public final class Util {
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  /**
   * a method to generate the hash key.
   *
   * @param key - the key from which we generate the hash key
   * @return - the generated hash key
   */
  public static String hashKey(String key) {
    String cacheKey;
    try {
      final MessageDigest mDigest = MessageDigest.getInstance("MD5");
      mDigest.update(key.getBytes(UTF_8));
      cacheKey = bytesToHexString(mDigest.digest());
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      cacheKey = String.valueOf(key.hashCode());
    }
    return cacheKey;
  }

  private static String bytesToHexString(byte[] bytes) {
    // http://stackoverflow.com/questions/332079
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bytes.length; i++) {
      String hex = Integer.toHexString(0xFF & bytes[i]);
      if (hex.length() == 1) {
        sb.append('0');
      }
      sb.append(hex);
    }
    return sb.toString();
  }
}

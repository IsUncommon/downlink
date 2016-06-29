[![](https://jitpack.io/v/isuncommon/downlink.svg)](https://jitpack.io/#isuncommon/downlink)

Downlink is simple file download management utility that can be embedded into your android projects.

####Goals
1. Simple and easy interface.
2. Serialized downloading.
3. Manage download directory limits.

####Usage
Create an instance of Downlink in your android Application object.
```
public class MainApplication extends Application {
private static final int DEFAULT_CACHE_MAX_SIZE_IN_BYTES = 250 * 1024 * 1024;
//--
@Override public void onCreate() {
    super.onCreate();
    File downloadDir = new File(getFilesDir(), "downloads");
    downlink = Downlink.create(downloadDir.getAbsolutePath(), DEFAULT_CACHE_MAX_SIZE_IN_BYTES);
    //optinally set logger
    downlink.setLogger(new Logger() {
      @Override public void log(String message) {
        //log message
      }
    });
  }
//--
}
```
Queue a url for download
```
downlink.queue("pubic-download-url");
```
Listen to url download status
```
downlink.addListener("pubic-download-url", new DownlinkListener() {
      @Override public void onCompleted(String url) {
        
      }

      @Override public void onQueued(String url) {

      }

      @Override public void onFailure(String url, Throwable t) {

      }

      @Override public void onProgress(String url, long downloadSize, long totalSize) {

      }

      @Override public void onCanceled(String url) {

      }
    });
```
Check download status or cancel a download.
```
downlink.downloadStatus("pubic-download-url")
//.....
downlink.cancelDownload("pubic-download-url");
```

###Open downloaded files.
You can use android FileProvider to serve the downloaded files.
AndroidManifest.xml
```
<provider
        android:name="android.support.v4.content.FileProvider"
        android:authorities="@string/downlink_authority"
        android:exported="false"
        android:grantUriPermissions="true">

      <meta-data
          android:name="android.support.FILE_PROVIDER_PATHS"
          android:resource="@xml/file_paths"/>
</provider>
```    
xml/files_paths.xml
```
<?xml version="1.0" encoding="utf-8"?>
<paths>
  <files-path
      name="downloads"
      path="downloads/"/>
</paths>
```
Get open intent from Downlink instance.
```
Intent intent = downlink.openIntent(context, authority, url);
try {
    context.startActivity(intent);
} catch (ActivityNotFoundException anfe) {
//
}
```
###Sample
Provided sample application details complete usage of Downlink.

###Misc
Downlink is powered by okhttp, you can supply your own okhttp client during downlink creation.
Clear or Reset to new size.
```
//clear
downlink.clear();
//reset
downlink.resetMaxSize(newSize);
```

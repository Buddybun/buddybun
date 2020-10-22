package com.example.meemansha;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.callback.Callback;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;
    ProgressDialog progressDialog;
    SwipeRefreshLayout swipeRefreshLayoutOne;

    final Activity activity = this;

    private static String file_type     = "*/*";    // file types to be allowed for upload
    private boolean multiple_files      = true;         // allowing multiple file upload

    /*-- MAIN VARIABLES --*/

    private static final String TAG = MainActivity.class.getSimpleName();

    private String cam_file_data = null;        // for storing camera file information
    private ValueCallback<Uri> file_data;       // data/header received after file selection
    private ValueCallback<Uri[]> file_path;     // received file(s) temp. location

    private final static int file_req_code = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;

            /*-- if file request cancelled; exited camera. we need to send null value to make future attempts workable --*/
            if (resultCode == Activity.RESULT_CANCELED) {
                if (requestCode == file_req_code) {
                    file_path.onReceiveValue(null);
                    return;
                }
            }

            /*-- continue if response is positive --*/
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == file_req_code){
                    if(null == file_path){
                        return;
                    }

                    ClipData clipData;
                    String stringData;
                    try {
                        clipData = intent.getClipData();
                        stringData = intent.getDataString();
                    }catch (Exception e){
                        clipData = null;
                        stringData = null;
                    }

                    if (clipData == null && stringData == null && cam_file_data != null) {
                        results = new Uri[]{Uri.parse(cam_file_data)};
                    }else{
                        if (clipData != null) { // checking if multiple files selected or not
                            final int numSelectedFiles = clipData.getItemCount();
                            results = new Uri[numSelectedFiles];
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                results[i] = clipData.getItemAt(i).getUri();
                            }
                        } else {
                            results = new Uri[]{Uri.parse(stringData)};
                        }
                    }
                }
            }
            file_path.onReceiveValue(results);
            file_path = null;
        }else{
            if(requestCode == file_req_code){
                if(null == file_data) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                file_data.onReceiveValue(result);
                file_data = null;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myWebView = (WebView) findViewById(R.id.webView);
        assert myWebView != null;

        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true); //alert of javascript

        myWebView.getSettings().setAppCacheEnabled(false);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //CookieSyncManager.createInstance(this);
        //CookieManager cookieManager = CookieManager.getInstance();
        //cookieManager.removeAllCookies(callback);
        //cookieManager.setAcceptCookie(false);



        myWebView.getSettings().setSaveFormData(false);
        myWebView.getSettings().setSavePassword(false);

        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setLoadsImagesAutomatically(true);

        myWebView.getSettings().setAllowFileAccess(true);//for file allow

        myWebView.getSettings().setUserAgentString("buddybun");// for gmail chrome permisiion
        //progressBarWeb = (ProgressBar) findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //progressDialog.setMessage("Please wait...");


        //For Remove error Web page not available and ERR_Cache_MISS

        if (18 < Build.VERSION.SDK_INT ){
            //18 = JellyBean MR2, KITKAT=19
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        //ProgressBar and Loading...

       /*myWebView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                //progressBarWeb.setVisibility(View.VISIBLE);
                //progressBarWeb.setProgress(newProgress);
                setTitle("Loading...");
                progressDialog.show();
                if(newProgress ==100){
                    //progressBarWeb.setVisibility(View.GONE);
                    setTitle(view.getTitle());
                    progressDialog.dismiss();

                }

                super.onProgressChanged(view, newProgress);
            }
        });*/
        //WebSettings webSettings = myWebView.getSettings();

        if(Build.VERSION.SDK_INT >= 21){
            myWebView.getSettings().setMixedContentMode(0);
            myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else if(Build.VERSION.SDK_INT >= 19){
            myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }else {
            myWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        myWebView.setWebViewClient(new Callback());

        myWebView.loadUrl("https://buddybun.com/");

        //File Upload code--------------------

        myWebView.setWebChromeClient(new WebChromeClient() {
            //ProgressBar and Loading...
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                //progressBarWeb.setVisibility(View.VISIBLE);
                //progressBarWeb.setProgress(newProgress);
//                setTitle("Please wait...");
                progressDialog.show();
                if(newProgress ==100){
                    //progressBarWeb.setVisibility(View.GONE);
                    setTitle(view.getTitle());
                    progressDialog.dismiss();

                }

                super.onProgressChanged(view, newProgress);
            }

            /*--
            openFileChooser is not a public Android API and has never been part of the SDK.
            handling input[type="file"] requests for android API 16+; I've removed support below API 21 as it was failing to work along with latest APIs.
            --*/
        /*    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                file_data = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(file_type);
                if (multiple_files) {
                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(Intent.createChooser(i, "File Chooser"), file_req_code);
            }
        */
            /*-- handling input[type="file"] requests for android API 21+ --*/
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                if(file_permission() && Build.VERSION.SDK_INT >= 21) {
                    file_path = filePathCallback;
                    Intent takePictureIntent = null;
                    Intent takeVideoIntent = null;

                    boolean includeVideo = false;
                    boolean includePhoto = false;

                    /*-- checking the accept parameter to determine which intent(s) to include --*/
                    paramCheck:
                    for (String acceptTypes : fileChooserParams.getAcceptTypes()) {
                        String[] splitTypes = acceptTypes.split(", ?+"); // although it's an array, it still seems to be the whole value; split it out into chunks so that we can detect multiple values
                        for (String acceptType : splitTypes) {
                            switch (acceptType) {
                                case "*/*":
                                    includePhoto = true;
                                    includeVideo = true;
                                    break paramCheck;
                                case "image/*":
                                    includePhoto = true;
                                    break;
                                case "video/*":
                                    includeVideo = true;
                                    break;
                            }
                        }
                    }

                    if (fileChooserParams.getAcceptTypes().length == 0) {   //no `accept` parameter was specified, allow both photo and video
                        includePhoto = true;
                        includeVideo = true;
                    }

                    if (includePhoto) {
                        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                            File photoFile = null;
                            try {
                                photoFile = create_image();
                                takePictureIntent.putExtra("PhotoPath", cam_file_data);
                            } catch (IOException ex) {
                                Log.e(TAG, "Image file creation failed", ex);
                            }
                            if (photoFile != null) {
                                cam_file_data = "file:" + photoFile.getAbsolutePath();
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            } else {
                                cam_file_data = null;
                                takePictureIntent = null;
                            }
                        }
                    }

                    if (includeVideo) {
                        takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        if (takeVideoIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                            File videoFile = null;
                            try {
                                videoFile = create_video();
                            } catch (IOException ex) {
                                Log.e(TAG, "Video file creation failed", ex);
                            }
                            if (videoFile != null) {
                                cam_file_data = "file:" + videoFile.getAbsolutePath();
                                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
                            } else {
                                cam_file_data = null;
                                takeVideoIntent = null;
                            }
                        }
                    }

                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType(file_type);
                    if (multiple_files) {
                        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    }

                    Intent[] intentArray;
                    if (takePictureIntent != null && takeVideoIntent != null) {
                        intentArray = new Intent[]{takePictureIntent, takeVideoIntent};
                    } else if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else if (takeVideoIntent != null) {
                        intentArray = new Intent[]{takeVideoIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooserIntent, file_req_code);
                    return true;
                } else {
                    return false;
                }
            }
        });





        //File code will continue(after on create will end)--------------

        //swipe refresh

        swipeRefreshLayoutOne = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayoutOne.setColorSchemeColors(Color.RED,Color.BLUE,Color.YELLOW);
        swipeRefreshLayoutOne.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myWebView.clearCache(true);
                //myWebView.clearHistory();

                myWebView.clearSslPreferences();
                //CookieManager.getInstance().removeAllCookies(null);
                //CookieManager.getInstance().flush();
                //U.clearCookies(getActivity());

                myWebView.reload();
            }
        });

        myWebView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (myWebView.getScrollY() == 0) {
                    swipeRefreshLayoutOne.setEnabled(true);
                } else {
                    swipeRefreshLayoutOne.setEnabled(false);
                }
            }
        });

        //myWebView.setWebViewClient(new WebViewClient());
        myWebView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayoutOne.setRefreshing(false);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        //cookieManager.setAcceptCookie(true);
    }


    //File upload continue----------

    /*-- callback reporting if error occurs --*/
    public class Callback extends WebViewClient{
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
        }
    }

    /*-- checking and asking for required file permissions --*/
    public boolean file_permission(){
        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            return false;
        }else{
            return true;
        }
    }

    /*-- creating new image file here --*/
    private File create_image() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    /*-- creating new video file here --*/
    private File create_video() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name    = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name     = "file_"+file_name+"_";
        File sd_directory   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".3gp", sd_directory);
    }

    /*-- back/down key handling --*/
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (myWebView.canGoBack()) {
                    myWebView.goBack();
                } else {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }



    //----------File upload code end


    //for back button (not of file)
    @Override


    public void onBackPressed() {
        if(myWebView.canGoBack())
        {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }


}
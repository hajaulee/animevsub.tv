package name.cpr;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.Window;
import cpr.name.videoenabledwebview.R;
import android.os.Build;
import android.content.pm.ActivityInfo;
import android.webkit.WebSettings;
import android.widget.*;
import android.graphics.*;
import java.util.*;
import android.content.*;
import java.io.*;
import android.view.*;

public class ExampleActivity extends ActionBarActivity{
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
	public String script = null;
	public LoadingDialog dialog;
	boolean loadingFinished = true;
	boolean redirect = false;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
							 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (android.os.Build.VERSION.SDK_INT >= 14){
			//noinspection all
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		} 
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getSupportActionBar().hide();

        setContentView(R.layout.activity_example);
        // Save the web view
        webView = (VideoEnabledWebView)findViewById(R.id.webView);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup)findViewById(R.id.videoLayout); // Your own view, read class comments
        //noinspection all
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress){
                // Your code...
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
			{
				@Override
				public void toggledFullscreen(boolean fullscreen){
					// Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
					if (fullscreen){
						WindowManager.LayoutParams attrs = getWindow().getAttributes();
						attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
						attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
						getWindow().setAttributes(attrs);
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

						if (android.os.Build.VERSION.SDK_INT >= 14){
							//noinspection all
							getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
						}
					}else{
						WindowManager.LayoutParams attrs = getWindow().getAttributes();
						//attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
						attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
						getWindow().setAttributes(attrs);
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
						if (android.os.Build.VERSION.SDK_INT >= 14){
							//noinspection all
							getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
						}
					}

				}
			});
        webView.setWebChromeClient(webChromeClient);
        // Call private class InsideWebViewClient
        webView.setWebViewClient(new InsideWebViewClient());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}   
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setMinimumFontSize(10);
		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		webView.setVerticalScrollBarEnabled(false);

        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.getSettings().setPluginState(android.webkit.WebSettings.PluginState.ON);
        // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
        webView.loadUrl("http://animevsub.tv");

    }

    private class InsideWebViewClient extends WebViewClient{
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url){
			if (!loadingFinished){
				redirect = true;
            }

            loadingFinished = false;
			view.loadUrl(url);		
            return true;
        }
		@Override
		public void onPageFinished(WebView view, String url){
			//Toast.makeText(ExampleActivity.this, "Fini", Toast.LENGTH_LONG).show();
			if (!redirect){
				loadingFinished = true;
				//HIDE LOADING IT HAS FINISHED
				//if (dialog != null){dialog.hide();}
			}else{
				redirect = false; 
			}

		}

		@Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            super.onPageStarted(view, url, favicon);
			//SHOW LOADING IF IT ISNT ALREADY VISIBLE  
			loadingFinished = false;
			dialog = LoadingDialog.getInstance(ExampleActivity.this);
			LoadingDialog.show(ExampleActivity.this);
        }

		@Override
		public void onPageCommitVisible(WebView view, String url){
			//Toast.makeText(ExampleActivity.this, "Commit", Toast.LENGTH_LONG).show();

			Runnable task = new Runnable(){

				@Override
				public void run(){
					if (script != null){
						webView.loadUrl("javascript:(function(){" + script + "\nscriptRun('ok');})()");
					}
				}
			};
			if (script == null){
				LoadCloudFile loadJs = new LoadCloudFile(ExampleActivity.this, task);
				loadJs.execute("http://nagoya.ml/files/androidtv.js");
			}else{
				//Toast.makeText(ExampleActivity.this, script, Toast.LENGTH_LONG).show();
				task.run();
			}

			new Timer().schedule(new TimerTask(){
					@Override
					public void run(){
						ExampleActivity.this.runOnUiThread(new Runnable(){
								@Override
								public void run(){
									LoadingDialog.hide();
								}
							});
					}
				}, 1200);
		}
    }

    @Override
    public void onBackPressed(){
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed()){
			String url = webView.getUrl();
            if(url.contains("/tap")){
				webView.loadUrl(url.substring(0, url.indexOf("/tap") + 1));
			}else if(url.contains("/phim")){
				webView.loadUrl(url.substring(0, url.indexOf("/phim") + 1));
			}
			else if (webView.canGoBack()){
                webView.goBack();
            }else{
                // Standard back button implementation (for example this could close the app)
                super.onBackPressed();
            }
        }
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		deleteCache(this);
	}
	
	String jsOnKey(Object key){
		return "javascript:(function(){" +script +"\nonKeyReceive('" + String.valueOf(key) + "');})()";
	}
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        Toast.makeText(this, String.valueOf(event.getKeyCode()), Toast.LENGTH_SHORT).show();
		
        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            webView.loadUrl(jsOnKey(event.getKeyCode()));
			//Toast.makeText(this, event.getKeyCode() +"", Toast.LENGTH_LONG).show();
        }
        return super.dispatchKeyEvent(event);
    }
	
	
	
	
	public static void deleteCache(Context context) {
		try {
			File dir = context.getCacheDir();
			deleteDir(dir);
		} catch (Exception e) { e.printStackTrace();}
	}

	public static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
			return dir.delete();
		} else if(dir!= null && dir.isFile()) {
			return dir.delete();
		} else {
			return false;
		}
	}

}

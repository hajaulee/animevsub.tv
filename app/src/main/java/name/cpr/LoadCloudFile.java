package name.cpr;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.support.v7.app.ActionBarActivity;
public class LoadCloudFile extends AsyncTask<String, String, String> {
    public static final String TAG = LoadCloudFile.class.getSimpleName();
    private Runnable task;
	private ExampleActivity sender;
    private HttpURLConnection urlConnection;

    public LoadCloudFile(ExampleActivity ac, Runnable callback){
		this.sender = ac;
        this.task = callback;
    }
    @Override
    protected String doInBackground(String... urls) {
        Log.d(TAG, "Started AsyncTask");
        String data = "";
        try {
            URL url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            data = convertStreamToString(in);
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, data);
            Log.e(TAG, e.toString());
        } finally {
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String resp) {
        super.onPostExecute(resp);
        Log.d(TAG, "onPostExecute");
		sender.script = resp;
        task.run();
    }
	public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
	
}

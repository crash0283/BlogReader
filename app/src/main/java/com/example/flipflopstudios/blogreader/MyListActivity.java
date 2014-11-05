package com.example.flipflopstudios.blogreader;

import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MyListActivity extends ListActivity {

    protected String[] mBlogPostTitles;
    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MyListActivity.class.getSimpleName();
    protected JSONObject mBlogData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        if(isNetworkAvailable()) {

            GetBlogPostsTasks blogPostsTasks = new GetBlogPostsTasks();
            blogPostsTasks.execute();

        }
        else {

            Toast.makeText(this,"Network is unavailable!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {

            isAvailable = true;
        }

        return isAvailable;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateList() {

        if (mBlogData == null) {
            //TODO:  Handle error


        }
        else {
            try {
                JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                mBlogPostTitles = new String[jsonPosts.length()];
                for (int i = 0; i < jsonPosts.length(); i++) {

                    JSONObject post = jsonPosts.getJSONObject(i);
                    String title = post.getString("title");
                    title = Html.fromHtml(title).toString();
                    mBlogPostTitles[i] = title;

                }

                ArrayAdapter <String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mBlogPostTitles);
                setListAdapter(adapter);

            } catch (JSONException e) {
                Log.e(TAG,"Exception Caught!",e);
            }
        }


    }

    private class GetBlogPostsTasks extends AsyncTask<Void,Void,JSONObject> {


        @Override
        protected JSONObject doInBackground(Void... params) {

            int responseCode = -1;
            JSONObject jsonResponse = null;


            try {
                URL blogFeedUrl = new URL(" http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);

                    int nextCharacter; // read() returns an int, we cast it to char later
                    String responseData = "";
                    while(true){ // Infinite loop, can only be stopped by a "break" statement
                        nextCharacter = reader.read(); // read() without parameters returns one character
                        if(nextCharacter == -1) // A return value of -1 means that we reached the end
                            break;
                        responseData += (char) nextCharacter; // The += operator appends the character to the end of the string
                    }


                    jsonResponse = new JSONObject(responseData);

                 }
                else {
                    Log.i(TAG, "Unsuccessful Http Response Code: " + responseCode);


                }
            }
            catch (MalformedURLException e) {

                Log.e(TAG, "Exception Caught!", e);

            }
            catch (IOException e) {

                Log.e(TAG, "Exception Caught!", e);

            }
            catch (Exception e) {

                Log.e(TAG, "Exception Caught!", e);
            }

            return jsonResponse;

        }
        @Override
        protected void onPostExecute(JSONObject result) {
            mBlogData = result;
            updateList();

        }
    }


}

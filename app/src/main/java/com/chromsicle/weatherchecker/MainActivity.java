package com.chromsicle.weatherchecker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView conditionsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        conditionsView = findViewById(R.id.conditionsView);
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            //convert the string into a URL
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();//creates a URL connection
                InputStream in = urlConnection.getInputStream(); //creates an input stream to collect the data as it comes through
                InputStreamReader reader = new InputStreamReader(in); //reads the data
                int data = reader.read();

                //keep reading data until it's all read
                //convert the int data that the reader is returning into a char and keep adding it to the result string
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find weather :(", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        //doInBackground should never touch UI stuff, onPostExecute is where that would go
        //this is where you write code that will execute when doInBackground finishes
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //convert the html to a JSON object
            try {
                JSONObject jsonObject = new JSONObject(s);

                String weatherInfo = jsonObject.getString("weather");
                Log.i("weather content", weatherInfo);

                //info from the weather object may be an array so we have to handle it appropriately
                JSONArray arr = new JSONArray(weatherInfo);

                String message = "";

                for(int i = 0; i < arr.length(); i++) {
                    JSONObject jsonPart = arr.getJSONObject(i);

                    String main = jsonPart.getString("main");
                    String description = jsonPart.getString("description");
                    if(!main.equals("") && !description.equals("")) {
                        message += main + ": " + description + "\r\n";
                    }
                }

                if(!message.equals("")) {
                    conditionsView.setText(message);
                } else {
                    Toast.makeText(getApplicationContext(), "Could not find weather :(", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Could not find weather :(", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
    }

    public void getWeather(View view){
        try {
            DownloadTask task = new DownloadTask();
            //deal with spaces in city names (was happening automatically but if it wasn't, use this)
            String encodedCityName = URLEncoder.encode(editText.getText().toString(), "UTF-8");
            //enter whatever the user entered into the url
            task.execute("https://openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&appid=b6907d289e10d714a6e88b30761fae22");

            //get rid of the keyboard when the check weather button is clicked
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not find weather :(", Toast.LENGTH_SHORT).show();
        }
    }
}

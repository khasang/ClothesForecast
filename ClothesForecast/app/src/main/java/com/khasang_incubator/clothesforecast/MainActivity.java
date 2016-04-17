package com.khasang_incubator.clothesforecast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.khasang_incubator.clothesforecast.helpers.Logger;
import com.khasang_incubator.clothesforecast.helpers.RequestMaker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvResponse;
    private EditText etCityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

    }

    private void initUI() {
        tvResponse = (TextView) findViewById(R.id.tvResponse);
        etCityName = (EditText) findViewById(R.id.etCityName);
        ((Button) findViewById(R.id.btnFetch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etCityName.getText().toString().trim().isEmpty()) {
                    new FetchForecastTask(etCityName.getText().toString()).execute();
                } else {
                    Toast.makeText(MainActivity.this, "Enter City Name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onResponseReceived(String response) {
        tvResponse.setText(response);

        Gson gson = new Gson();
        WeatherResponse weatherResponse = gson.fromJson(response, WeatherResponse.class);
        Coordinate coordinate = weatherResponse.getCoord();
        Logger.d(String.format("coord: (%f %f)", coordinate.getLon(), coordinate.getLat()));
        Weather weather = weatherResponse.getWeather().get(0);
        Logger.d(String.format("id: %d\nmain: %s\ndesc: %s\nicon: %s",
                weather.getId(), weather.getMain(), weather.getDescription(), weather.getIcon()));
    }

    private class FetchForecastTask extends AsyncTask<Void, Void, String> {
        private String cityName;
        private String request;

        public FetchForecastTask(String cityName) {
            this.cityName = cityName;
        }

        @Override
        protected void onPreExecute() {
            request = RequestMaker.getWeatherFor(cityName);
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = null;
            try {
                response = new String(getUrlBytes(request));
                Logger.d(response);
            } catch (IOException e) {
                e.printStackTrace();
                response = "Something Wrong";
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            onResponseReceived(s);
        }

        private byte[] getUrlBytes(String urlSpec) throws IOException {
            URL url = new URL(urlSpec);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = connection.getInputStream();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                return out.toByteArray();
            } finally {
                connection.disconnect();
            }

        }
    }
}
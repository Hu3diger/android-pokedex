package com.example.pokedex;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private String responseJSON;
    private Button btn;
    private ImageView imgView;
    private TextView txtView;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        btn = findViewById(R.id.button);
        imgView = findViewById(R.id.imageView2);
        txtView = findViewById(R.id.textView);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadJSON();
            }
        });
    }


    public void downloadJSON() {
        URL url;
        StringBuffer response = new StringBuffer();
        try {
            url = new URL("https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/master/pokedex.json");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url");
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

            //Here is your json in string format
            responseJSON = response.toString();
            loadJSONobject();
        }
    }

    private void loadJSONobject() {
        String data = "";
        try {
            JSONObject jsonRootObject = new JSONObject(responseJSON);

            JSONArray jsonArray = jsonRootObject.optJSONArray("pokemon");

            Random random = new Random();
            int index = random.nextInt(150)+1;

            JSONObject jsonObject = jsonArray.getJSONObject(index);
            int id = Integer.parseInt(jsonObject.optString("id").toString());
            String name = jsonObject.optString("name").toString();
            String img = jsonObject.optString("img").toString();
            downloadImage(img);
            txtView.setText("id: " + id + " - nome: " + name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void downloadImage(String imageUrl) {
        ImageDownloader imageDownloader = new ImageDownloader();
        imageUrl = imageUrl.replace("http", "https");
        try {
            Bitmap imagem = imageDownloader.execute(imageUrl).get();
            imgView.setImageBitmap(imagem);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

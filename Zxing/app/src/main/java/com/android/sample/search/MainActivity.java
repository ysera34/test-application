package com.android.sample.search;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String CLIENT_ID = "fNS5Jc263TJmUPOMV8OV";
    private static final String CLIENT_SECRET = "XMAEJ7ajhb";

    private String mISBNText;
    private String mName;
    private Button mStartButton;
    private Button mISBNRequestButton;
    private Button mNaverRequestButton;
    private TextView mISBNTextView;
    private TextView mProductNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(this);
        mISBNRequestButton = (Button) findViewById(R.id.isbn_request_button);
        mISBNRequestButton.setOnClickListener(this);
        mNaverRequestButton = (Button) findViewById(R.id.naver_request_button);
        mNaverRequestButton.setOnClickListener(this);
        mISBNTextView = (TextView) findViewById(R.id.isbn_text_view);
        mProductNameTextView = (TextView) findViewById(R.id.product_name_text_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_button:
                new IntentIntegrator(this).initiateScan();
                break;
            case R.id.isbn_request_button:
                if (mISBNText != null) {
                    new ISBNRequestTask().execute(mISBNText);
                } else {
                    Toast.makeText(this, "바코드를 검색하세요", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.naver_request_button:
                if (mName != null) {
                    new NaverRequestTask().execute(mName);
                } else {
                    Toast.makeText(this, "이름을 요청하세요", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        mISBNText = result.getContents();
        mISBNTextView.setText(getString(R.string.isbn_format, mISBNText));
        Toast.makeText(this, "ISBN:" + mISBNText, Toast.LENGTH_SHORT).show();
    }

    private class ISBNRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = "http://www.koreannet.or.kr/home/hpisSrchGtin.gs1?gtin="+params[0];
            Log.i("ISBNRequestTask url", url);
            String html = null;

            try {
                /*
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                in.close();
                connection.disconnect();
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                */
                Document doc = Jsoup.connect(url).get();
                Elements ele = doc.select("div.productTit");
                // html = ele.toString();
                Log.i("html", ele.toString());
                html = ele.text();
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: exception:" + e.getMessage());
            }
            return html;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // s = s.split("\\s+")[1];
            s = s.replace("&nbsp;", "");
            Log.i(TAG, "onPostExecute: s:" + s);
            s = s.replaceFirst("^ *", "");
            Log.i(TAG, "onPostExecute: s:" + s);
            mName = s;
            mProductNameTextView.setText(getString(R.string.product_name_format, s));
        }
    }

    private class NaverRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = "https://openapi.naver.com/v1/search/shop.json?query="+params[0];
            Log.i("NaverRequestTask url", url);

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
                connection.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);

                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    Log.i(TAG, "line: " + line + "\n");
                    stringBuilder.append(line).append("\n");
                }
                in.close();
                connection.disconnect();
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
//                Log.d(TAG, "doInBackground: jsonObject:" + jsonObject.toString());

            } catch (Exception e) {
                Log.e(TAG, "doInBackground: exception:" + e.getMessage());
            }
            return "";
        }
    }
}

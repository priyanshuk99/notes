package com.example.firstapp;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
//    private Button start,stop,logout;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009;

    SharedPreferenceClass sharedPreferenceClass;
    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        if(id== R.id.search){
            Toast.makeText(getApplicationContext(),"search clicked",Toast.LENGTH_SHORT).show();
            if(searchView.getVisibility()==View.VISIBLE){
                searchView.setVisibility(View.GONE);
            }else{
                searchView.setVisibility(View.VISIBLE);
            }
        }else if(id== R.id.stop){
            Toast.makeText(getApplicationContext(),"stop clicked",Toast.LENGTH_SHORT).show();
            stopService(new Intent(MainActivity.this, ScreenshotService.class));
        }if(id== R.id.start){
            Toast.makeText(getApplicationContext(),"start clicked",Toast.LENGTH_SHORT).show();
            startService(new Intent(MainActivity.this, ScreenshotService.class));
        }if(id== R.id.logout){
            Toast.makeText(getApplicationContext(),"logout clicked",Toast.LENGTH_SHORT).show();
            sharedPreferenceClass.clear();
            stopService(new Intent(MainActivity.this, ScreenshotService.class));
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
        checkReadExternalStoragePermission();

        searchView=findViewById(R.id.search);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferenceClass = new SharedPreferenceClass(this);



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                final HashMap<String, String> params = new HashMap<>();
                params.put("text", query);

                String apiKey = "https://notesandroid.herokuapp.com/api/notes/search";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")){
                                Toast.makeText(MainActivity.this,"Text found"+response,Toast.LENGTH_LONG).show();
                            }
//                    progressBar.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this,"No text found",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
//                    progressBar.setVisibility(View.GONE);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if(error instanceof ServerError && response !=null){
                            try {
                                String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers,"utf-8"));
                                JSONObject object= new JSONObject(res);
                                Toast.makeText(MainActivity.this, object.getString("msg"),Toast.LENGTH_LONG).show();

//                        progressBar.setVisibility(View.GONE);
                            }catch (JSONException| UnsupportedEncodingException je){
                                je.printStackTrace();
//                        progressBar.setVisibility(View.GONE);
                            }
                        }

                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                final int socketTime = 3000;
                RetryPolicy policy = new DefaultRetryPolicy(
                        socketTime, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                jsonObjectRequest.setRetryPolicy(policy);

                //request add
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                requestQueue.add(jsonObjectRequest);



                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showReadExternalStoragePermissionDeniedMessage();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    private void checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestReadExternalStoragePermission();
        }
    }

    private void requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
    }

    private void showReadExternalStoragePermissionDeniedMessage() {
        Toast.makeText(this, "Read external storage permission has denied", Toast.LENGTH_SHORT).show();
    }

}
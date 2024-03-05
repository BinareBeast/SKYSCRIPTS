package com.example.skyscripts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final int PERMISSION_CODE = 1;
    private RelativeLayout homerl;
    private ProgressBar progressBar;
    private TextView cityname, temeperature, condition;
    private RecyclerView rvweather;
    private TextInputEditText cityedt;
    private ImageView backimage, iconimage, searchimage;
    private ArrayList<WeatherRV> weatherRVArrayList;
    private WeatherRVadapter weatherRVadapter;
    private LocationManager locationManager;
    private String CityName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        homerl = findViewById(R.id.homerl);
        progressBar = findViewById(R.id.pbloading);
        cityname = findViewById(R.id.ctyname);
        temeperature = findViewById(R.id.temperature);
        condition = findViewById(R.id.condition);
        rvweather = findViewById(R.id.rvweather);
        backimage = findViewById(R.id.ivback);
        iconimage = findViewById(R.id.ivicon);
        searchimage = findViewById(R.id.ivsearch);
        weatherRVArrayList = new ArrayList<>();
        weatherRVadapter = new WeatherRVadapter(this, weatherRVArrayList);
        rvweather.setAdapter(weatherRVadapter);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        cityedt = findViewById(R.id.edcty);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        } else extractLocation();

        searchimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cty = cityedt.getText().toString();
                if (cty.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter a City Name", Toast.LENGTH_SHORT).show();
                } else {
                    cityname.setText(CityName);
                    getweatherinfo(cty);
                }
            }
        });

    }

    private void extractLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        GpsTracker gpsTracker = new GpsTracker(this);
        if (!gpsTracker.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            setUpLocation();
        gpsTracker.setGpsUpdateListener(gpsTracker1 -> {
            if (CityName != null) return;
            Location location1 = gpsTracker1.getLocation();
            CityName = getcityname(location1.getLatitude(), location1.getLongitude());
            if (CityName != null)
                getweatherinfo(CityName);
        });
    }

    private void setUpLocation() {
        log("LLL");
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            log("GPS Disabled");
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        extractLocation();
    }

    private void log(String s) {
        Log.e("t", s);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                extractLocation();
            } else {
                Toast.makeText(this, "Please provide the permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Nullable
    private String getcityname(Double lat, Double log) {
        String cityname = "notfound";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(lat, log, 10);
            for (Address adr : addresses) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityname = city;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (cityname.equals("notfound")) {
            Log.d("TAG", "CITY NOT FOUND");
            Toast.makeText(this, "USER CITY NOT FOUND..", Toast.LENGTH_SHORT).show();
            return null;
        }
        log(cityname);
        return cityname;
    }

    private void getweatherinfo(String cityname) {
        log("city " + cityname);
        String url = "https://api.weatherapi.com/v1/forecast.json?key=d9f6148514fd4983897160437232610&q=" + cityname + "&days=1&aqi=yes&alerts=yes";
        this.cityname.setText(cityname);

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                homerl.setVisibility(View.VISIBLE);
                weatherRVArrayList.clear();
//                try {
//                    log(response.getJSONObject("current").getDouble("temp_c") + " in log");
//                } catch (JSONException e) {
//                    log(e.toString());
//                }
                try {
                    String temp = response.getJSONObject("current").getString("temp_c");
                    temeperature.setText(temp + "°c");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String conditi = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionicon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:".concat(conditionicon)).into(iconimage);
                    condition.setText(conditi);
                    if (isDay == 1) {
                        Picasso.get().load("https://img.freepik.com/premium-photo/beautiful-blue-sky-with-clouds-rising-moon-daylight_298425-1622.jpg").into(backimage);
                    } else {
                        Picasso.get().load("https://e1.pxfuel.com/desktop-wallpaper/757/767/desktop-wallpaper-best-of-starry-night-sky-phone-rotwall-blogspot-starry-phone.jpg").into(backimage);
                    }

                    JSONObject forecastobject = response.getJSONObject("forecast");
                    JSONObject forecasto = forecastobject.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourarray = forecasto.getJSONArray("hour");

                    for (int i = 0; i < hourarray.length(); i++) {
                        JSONObject hourobject = hourarray.getJSONObject(i);
                        String time = hourobject.getString("time");
                        String temper = hourobject.getString("temp_c");
                        String img = hourobject.getJSONObject("condition").getString("icon");
                        String wind = hourobject.getString("wind_kph");
                        weatherRVArrayList.add(new WeatherRV(time, temper, img, wind));
                    }
                    weatherRVadapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    log(e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                log(error.toString());
                Toast.makeText(MainActivity.this, "Please enter a valid city", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
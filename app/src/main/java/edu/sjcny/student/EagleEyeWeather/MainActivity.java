package edu.sjcny.student.EagleEyeWeather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.friedrich.kaiser.weatherapp.R;

import org.json.JSONException;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import edu.sjcny.student.EagleEyeWeather.json.JSONParser;
import edu.sjcny.student.EagleEyeWeather.url.WeatherURLHandler;
import edu.sjcny.student.EagleEyeWeather.weather.WeatherObject;

public class MainActivity extends AppCompatActivity {

    public final String appKey = /*getString(R.string.api_key);*/ "2499865a319393f1770ce3daa85674da";
    public WeatherObject curWeather;
    public WeatherObject[] weekWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Location Start
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //   Consider calling
            //   ActivityCompat#requestPermissions
            //   here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            //   to handle the case where the user grants the permission. See the documentation
            //   for ActivityCompat#requestPermissions for more details.
            //   return TODO;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 321);
        }
        //TODO: check if location is on, and iff not, ask to turn on

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

        /**
         * this call ensures that onLocationChanged has been called at least once.
         */
        locationListener.onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        //Location End

        //This is the workaround for not using an Async Call
        //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        //TODO move everything to a method called in onLocationChanged???


    }

    /**
     * @param requestCode used in the switch statement, should match with the permission you resuested
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case 123: //Fine Location
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission has been granted
                    Log.d("PERMISSIONS", "Location permission granted");
                }
                else
                {
                    //permission not granted
                    //kill yourself D:
                }
                break;
            case 321: //Coarse Location
                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //permission has been granted
                    Log.d("PERMISSIONS", "Location permission granted");
                }
                else
                {
                    //permission not granted
                    //kill yourself D:
                }
        }
    }

    public void launchBrowser(View view)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.weather.com"));
        startActivity(browserIntent);
    }

    private void populateWeatherFields()
    {
        Calendar calendar = Calendar.getInstance();
        String[] week = getWeeklist(calendar.get(Calendar.DAY_OF_WEEK));


        char deg = (char) 0x00B0;//'\u0B00';//(char) 248;
        ((TextView)findViewById(R.id.Today_temp)).setText("Today: " + curWeather.getMinTemp() + deg + "/" + curWeather.getMaxTemp() + deg);
        ((TextView)findViewById(R.id.wind_spd)).setText("Windspeed: " + curWeather.getWindspeed());

        ((TextView)findViewById(R.id.Tom_temp)).setText("Tomorrow: " + weekWeather[1].getMinTemp() + deg + "/" + weekWeather[1].getMaxTemp() + deg);
        ((TextView)findViewById(R.id.day_3_temp)).setText(week[2] + ": " + weekWeather[2].getMinTemp() + deg + "/" + weekWeather[2].getMaxTemp() + deg);
        ((TextView)findViewById(R.id.day_4_temp)).setText(week[3] + ": " + weekWeather[3].getMinTemp() + deg + "/" + weekWeather[3].getMaxTemp() + deg);
        ((TextView)findViewById(R.id.day_5_temp)).setText(week[4] + ": " + weekWeather[4].getMinTemp() + deg + "/" + weekWeather[4].getMaxTemp() + deg);
        ((TextView)findViewById(R.id.day_6_temp)).setText(week[5] + ": " + weekWeather[5].getMinTemp() + deg + "/" + weekWeather[5].getMaxTemp() + deg);
        ((TextView)findViewById(R.id.day_7_temp)).setText(week[6] + ": " + weekWeather[6].getMinTemp() + deg + "/" + weekWeather[6].getMaxTemp() + deg);


        /*String[] weathers = new String[] {
                "Now: " + curWeather.getAvgTemp() + "\u00B0",
                "Today's High: " + curWeather.getMaxTemp() + "\u00B0 Low: " + curWeather.getMinTemp() + "\u00B0"};
                //"Tomorrow: " + temp2High + "" + temp2Low + "*",
                //"Thursday: " + temp3High + "" + temp3Low + "*" };

        /*ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, weathers);

        ListView listView = (ListView) findViewById(R.id.temps_listview);
        listView.setAdapter(adapter);
        listView.setDivider(null);*/
    }

    /**
     *
     * @param startday Sunday is 1
     * @return
     */
    private String[] getWeeklist(int startday)
    {
        --startday;
        String[] weekdays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        String[] result = new String[7];

        result[0] = weekdays[startday];
        int j = 1;
        while(j < result.length)
        {
            ++startday;
            if(startday >= result.length)
            {
                startday = 0;
            }
            result[j] = weekdays[startday];
            ++j;
        }
        return result;
    }

    /**
     * This is an interface that retrieves GPS location data
     */
    private class MyLocationListener implements LocationListener {

        String longitude = "";

        public String getLongitude() {
            return longitude;
        }

        String latitude = "";

        public String getLatitude() {
            return latitude;
        }

        @Override
        public void onLocationChanged(Location loc) {

            //TODO truncate the decimal after a few places
            longitude = Double.toString(loc.getLongitude());
            Log.d("LOC", longitude);
            latitude = Double.toString(loc.getLatitude());
            Log.d("LOC", latitude);

            //TODO reget JSON through API and reParse them, repopulate fields.

            //TODO Initialize the GUI and Weather Objects (Current, WeekAhead[])
            curWeather = new WeatherObject();
            weekWeather = new WeatherObject[7];

            //TODO have the chrome(?) browser open at the touch of a button to the Weather Channel page???

            //Begin Async Call
            //Use the Weather URL Handler in the Async Call to get JSON as String
            String jsonNow = "";
            String jsonAhead = "";


            //TODO: check if internet/data is on, and iff not, ask to turn on
            try {
                Log.d("THREAD","Start");
                WeatherTask weather = new WeatherTask();
                jsonNow = (weather.execute(WeatherURLHandler.getWeatherAPICALL(appKey, ((MyLocationListener) this).getLatitude(), ((MyLocationListener) this).getLongitude(), "imperial"))).get();
                weather.cancel(true);
                weather = new WeatherTask();
                jsonAhead = (weather.execute(WeatherURLHandler.getWeatherAPICALL(appKey, ((MyLocationListener) this).getLatitude(), ((MyLocationListener) this).getLongitude(), "imperial", 7))).get();
                weather.cancel(true);
                Log.d("THREAD","End, Meme Achieved");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            //end Async Call

        /*try {
            jsonNow = WeatherURLHandler.readUrl(WeatherURLHandler.getWeatherAPICALL(appKey, ((MyLocationListener) locationListener).getLatitude(), ((MyLocationListener) locationListener).getLongitude(), "imperial"));
            jsonAhead = WeatherURLHandler.readUrl(WeatherURLHandler.getWeatherAPICALL(appKey, ((MyLocationListener) locationListener).getLatitude(), ((MyLocationListener) locationListener).getLongitude(), "imperial", 7));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
            //parse JSON using JSONObject API into Weather Objects
            try {
                curWeather = JSONParser.parseJSONNow(jsonNow);
                weekWeather = JSONParser.parseJSONWeekAhead(jsonAhead);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //TODO Update the GUI Fields with information taken from the Weather Objects




        /*CurrentWeather currentWeather = null;
        WeeklyWeather weeklyWeather = null;
        WeatherURLHandler weatherRetriever = new WeatherURLHandler(currentWeather, weeklyWeather);*/

            populateWeatherFields();

        /*------- To get city name from coordinates -------- */
            /*String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //String s = longitude + "\n" + latitude + "\n\nMy Current City is: " + cityName;
            //editLocation.setText(s);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }


}

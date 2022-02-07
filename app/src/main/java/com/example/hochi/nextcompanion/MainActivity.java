package com.example.hochi.nextcompanion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AsyncTaskCallbacks<String> {
    private RequestHandler getBikesTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //now this "every android activity" stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Context context = this;

        //Floating Button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RentActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //pre-condition: Is there a login key?
        SharedPreferences sharedPref = getSharedPreferences("persistence", MODE_PRIVATE);
        String defaultValue = "nokey";
        String loginKey = sharedPref.getString("loginKey", defaultValue);
        //if not, go to LoginActivity
        if (loginKey.equals("nokey")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else {
            reloadBikeList();
            updatePersonalInformation();
        }
    }


    /**
     * Update text fields displaying the personal information (name, phone number) ...
     * ... for the logged in user based on the values currently stored in the sharedPreferences
     */
    private void updatePersonalInformation() {
        // Get values from sPrefs
        SharedPreferences sharedPref = getSharedPreferences("persistence", MODE_PRIVATE);
        String defaultValueName = getString(R.string.unknown);
        String username = sharedPref.getString("username", defaultValueName);
        String defaultValuePhone = getString(R.string.unknown);
        String phoneNumber = sharedPref.getString("phone", defaultValuePhone);

        // Find the views
        TextView tvUsername = findViewById(R.id.tv_display_username);
        TextView tvPhone = findViewById(R.id.tv_display_phone);

        // Get label-strings
        String labelUsername = getString(R.string.username);
        String labelPhone = getString(R.string.phone);

        // Update text content
        if(tvUsername!=null)
            tvUsername.setText(String.format("%s: %s", labelUsername, username));
        if(tvPhone!=null)
            tvPhone.setText(String.format("%s: %s", labelPhone, phoneNumber));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            SharedPreferences sharedPref = getSharedPreferences("persistence", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("loginKey");
            // Remove username
            editor.remove("username");
            // Remove phone number
            editor.remove("phone");
            editor.apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_map) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.map_url)));
            startActivity(browserIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void reloadBikeList() {
        //get loginkey
        SharedPreferences sharedPref = getSharedPreferences("persistence", MODE_PRIVATE);
        String defaultValue = "nokey";
        String loginKey = sharedPref.getString("loginKey", defaultValue);

        String[] params = {
                "apikey=", getString(R.string.apikey),
                "loginkey=", loginKey
        };

        getBikesTask = new RequestHandler(this, "POST",
                "api/getOpenRentals.json", params);
        getBikesTask.execute((Void) null);
    }

    @Override
    public void onTaskComplete(String response) {
        //Callback called when RequestHandler finished request
        final Context context = this;
        if (!response.isEmpty()) {
            final ArrayList<String> list = new ArrayList<>();
            try {
                JSONObject jObject = new JSONObject(response);
                JSONArray bikesArray = jObject.getJSONArray("rentalCollection");
                for (int i = 0; i < bikesArray.length(); i++) {
                    String entry;
                    JSONObject bike = bikesArray.getJSONObject(i);
                    entry = "Bike " + bike.getString("bike")
                            + " with lock code " + bike.getString("code");
                    list.add(entry);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Create and fill list
            final ListView listview = findViewById(R.id.listview);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, list);
            listview.setAdapter(adapter);

            //Print indicator if empty
            TextView tv = findViewById(R.id.noBikes);
            if(list.isEmpty()) tv.setVisibility(View.VISIBLE);
            else tv.setVisibility(View.INVISIBLE);

            try {
                final JSONObject jObject = new JSONObject(response);
                final JSONArray bikesArray = jObject.getJSONArray("rentalCollection");
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    Intent intent = new Intent(context, ReturnActivity.class);
                    try {
                        JSONObject bike = bikesArray.getJSONObject(position);
                        String bID = bike.getString("bike");
                        String stID = bike.getString("start_place");
                        String lockE = bike.getString("electric_lock");
                        String[] bikeArray = {bID, stID, lockE};
                        intent.putExtra("bike", bikeArray);
                        startActivity(intent);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            //TODO: implement error handling
        }
    }
}

package com.stenden.esselbrugge.stendenrooster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    List<Map<String, String>> CurrentFavo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Add schedule");

        getSearchItems("");

        try {
            File file = new File(getDir("data", MODE_PRIVATE), "favorites");
            ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
            CurrentFavo = (List<Map<String, String>>)inputstream.readObject();
            inputstream.close();
        }catch (Exception e){

        }

        Log.d("GOODDDD",CurrentFavo.toString());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_search && item.getTitle().equals("Cancel")){
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);

            item.setIcon(R.drawable.ic_search_black_24dp);
            item.setTitle("Add schedule");
            return true;
        }

        if (id == R.id.action_search) {
            item.setIcon(R.drawable.ic_cancel_black_24dp);
            item.setTitle("Cancel");
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflator.inflate(R.layout.menu_search, null);

            actionBar.setCustomView(v);

            final EditText editText = (EditText) findViewById(R.id.searchBar);

            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        item.setIcon(R.drawable.ic_search_black_24dp);
                        actionBar.setDisplayShowCustomEnabled(false);
                        actionBar.setDisplayShowTitleEnabled(true);
                        item.setTitle("Search");

                        getSupportActionBar().setTitle("Searchresults: " +editText.getText().toString());

                        getSearchItems(editText.getText().toString());

                        return true;
                    }
                    return false;
                }
            });

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void loginButton(View v){
        Intent stenden = new Intent(SearchActivity.this, StendenLogin.class);
        startActivityForResult(stenden, 2);
    }

    public void getSearchItems(final String searchterm) {
        View C = findViewById(R.id.content);
        ViewGroup parent1 = (ViewGroup) C.getParent();
        int index = parent1.indexOfChild(C);
        parent1.removeView(C);
        C = getLayoutInflater().inflate(R.layout.sidebar_loading, parent1, false);
        parent1.addView(C, index);

        try {

            new Thread() {
                public void run() {
                    final ServerConnection con = new ServerConnection();
                    final JSONArray data = con.getGroups(getApplicationContext());
                    final JSONArray data2 = con.getRooms(getApplicationContext());
                    final JSONArray data3 = con.getTeachers(getApplicationContext());

                    try {
                        if(data.length() == 0){
                            Intent stenden = new Intent(SearchActivity.this, StendenLogin.class);
                            startActivityForResult(stenden, 2);
                        }
                    }catch (Exception e){
                        Log.e("d",e.toString());
                    }

                    SearchActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            addSearchItems(data,data2,data3,searchterm);
                        }
                    });
                }
            }.start();

        }catch (Exception e){

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 2) {
            getSearchItems("");

            try {
                File file = new File(getDir("data", MODE_PRIVATE), "favorites");
                ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
                CurrentFavo = (List<Map<String, String>>)inputstream.readObject();
                inputstream.close();
            }catch (Exception e){

            }
        }
    }

    public void addSearchItems(JSONArray json, JSONArray json2,JSONArray json3,String searchterm) {

        View C = findViewById(R.id.content);
        ViewGroup parent1 = (ViewGroup) C.getParent();
        int index = parent1.indexOfChild(C);
        parent1.removeView(C);
        C = getLayoutInflater().inflate(R.layout.search_main, parent1, false);
        parent1.addView(C, index);

        final List<Map<String, String>> data = new ArrayList<>();
        ListView list = (ListView)findViewById(R.id.SearchList);

        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject row = json.getJSONObject(i);
                Map<String, String> item = new HashMap<>(2);

                if (row.getString("code").toLowerCase().contains(searchterm.toLowerCase())) {
                    item.put("Name", row.getString("code"));
                    item.put("ID", row.getString("id"));
                    item.put("Type", "1");
                    data.add(item);
                }

            } catch (Exception e) {
                Log.d("error", e.toString());
            }
        }

        for (int i = 0; i < json2.length(); i++) {
            try {
                JSONObject row = json2.getJSONObject(i);
                Map<String, String> item = new HashMap<>(2);

                if (row.getString("code").toLowerCase().contains(searchterm.toLowerCase())) {
                    item.put("Name", row.getString("code"));
                    item.put("ID", row.getString("id"));
                    item.put("Type", "2");
                    data.add(item);
                }

            } catch (Exception e) {
                Log.d("error", e.toString());
            }
        }

        for (int i = 0; i < json3.length(); i++) {
            try {
                JSONObject row = json3.getJSONObject(i);
                Map<String, String> item = new HashMap<>(2);

                if (row.getString("code").toLowerCase().contains(searchterm.toLowerCase())) {
                    item.put("Name", row.getString("code"));
                    item.put("ID", row.getString("id"));
                    item.put("Type", "3");
                    data.add(item);
                }

            } catch (Exception e) {
                Log.d("error", e.toString());
            }
        }

        ListAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_sidebar,
                new String[]{"Name"},
                new int[]{R.id.text1}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView ev = view.findViewById(R.id.icon);

                if(data.get(position).get("Type").equals("1")) {
                    ev.setImageResource(R.drawable.ic_group_black_24dp);
                }
                if(data.get(position).get("Type").equals("2")) {
                    ev.setImageResource(R.drawable.ic_location_sidebar);
                }
                if(data.get(position).get("Type").equals("3")) {
                    ev.setImageResource(R.drawable.ic_person_black_24dp);
                }
                return view;
            }
        };
        list.setAdapter(adapter);

        if(data.size() == 0){
            parent1.removeView(C);
            C = getLayoutInflater().inflate(R.layout.sidebar_error, parent1, false);
            parent1.addView(C, index);
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Map<String, String> item;
                    item = data.get(position);
                    CurrentFavo.add(item);

                    File file = new File(getDir("data", MODE_PRIVATE), "favorites");
                    ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                    outputStream.writeObject(CurrentFavo);
                    outputStream.flush();
                    outputStream.close();

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("NewItem","OK");
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }catch (Exception e){

                }
            }
        });
    }

    }

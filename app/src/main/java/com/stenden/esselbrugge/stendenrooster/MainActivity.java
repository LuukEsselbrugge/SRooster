package com.stenden.esselbrugge.stendenrooster;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    TabLayout tabLayout;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ListView mDrawerList;
    Calendar pickedDate;
    String CurrentWeekNumber;
    ArrayList<String> CurrentIDs = new ArrayList<String>();
    Boolean compareMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.NavList);
        addDrawerItems();
        addFavorites();
        setupDrawer();

        loginGood();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setElevation(0);

//        SharedPreferences SP = getApplicationContext().getSharedPreferences("Cookies", MODE_PRIVATE);
//        SP.edit().putString("Cookie","yeet").commit();

    }

    public void loginButton(View v){
        Intent stenden = new Intent(MainActivity.this, StendenLogin.class);
        startActivityForResult(stenden, 2);
    }

    private void offlineModeCheck(){

        new Thread() {
            public void run() {
                final ServerConnection con = new ServerConnection();
                final JSONArray data = con.checkMode(getApplicationContext());

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if(data.length() == 0){
                               LinearLayout offline = (LinearLayout) findViewById(R.id.offlineMode);
                                Button offlineButton = findViewById(R.id.offlineButton);
                                TextView t = findViewById(R.id.TextError);
                                if(offlineButton != null){
                                    offlineButton.setVisibility(View.VISIBLE);
                                    t.setText("Your session has expired");
                                }
                                offline.setVisibility(View.VISIBLE);
                            }
                        }catch (Exception e){
                            Log.e("d",e.toString());
                        }
                    }
                });

            }
        }.start();
    }

    private void checkLogin(){
        View C = findViewById(R.id.content);
        ViewGroup parent1 = (ViewGroup) C.getParent();
        int index = parent1.indexOfChild(C);
        parent1.removeView(C);
        C = getLayoutInflater().inflate(R.layout.sidebar_loading, parent1, false);
        parent1.addView(C, index);

        new Thread() {
            public void run() {
                final ServerConnection con = new ServerConnection();

                final JSONArray data = con.getGroups(getApplicationContext());

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                           if(data.length() == 0){
                               Intent stenden = new Intent(MainActivity.this, StendenLogin.class);
                               startActivityForResult(stenden, 2);
                           }else{
                               loginGood();
                           }
                        }catch (Exception e){
                            Log.e("d",e.toString());
                        }
                    }
                });

            }
        }.start();
    }

    private void loginGood(){
        pickedDate = Calendar.getInstance();

        String CurrentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        List<Map<String, String>> CurrentFavo;
        CurrentIDs.clear(); // fix voor dubbel items lol xd end my life

        try {
            File file = new File(getDir("data", MODE_PRIVATE), "favorites");
            ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
            CurrentFavo = (List<Map<String, String>>)inputstream.readObject();

            inputstream.close();

            CurrentIDs.add(0,CurrentFavo.get(0).get("ID"));
            getSchedule(CurrentDate,CurrentIDs);
            getSupportActionBar().setTitle(CurrentFavo.get(0).get("Name"));
        }catch (Exception e){
            Intent myIntent = new Intent(MainActivity.this, SearchActivity.class);
            startActivityForResult(myIntent, 1);
            View C = findViewById(R.id.content);
            ViewGroup parent1 = (ViewGroup) C.getParent();
            int index = parent1.indexOfChild(C);
            parent1.removeView(C);
            C = getLayoutInflater().inflate(R.layout.sidebar_error, parent1, false);
            parent1.addView(C, index);

            TextView errortext = (TextView)findViewById(R.id.TextError);
            Button b = C.findViewById(R.id.button);
            b.setVisibility(View.GONE);
            errortext.setText("Please select a schedule from the sidebar");
        }
    }

    private void addDrawerItems() {
        String[] menuArray = {"Settings","Information"};
        final Integer[] menuIconArray = {R.drawable.ic_settings_black_24dp,R.drawable.ic_help_black_24dp};

        List<Map<String, String>> data = new ArrayList<>();

        for (String menuItem : menuArray) {
            Map<String, String> item = new HashMap<>(2);
            item.put("First Line", menuItem);
            data.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_sidebar,
                new String[]{"First Line"},
                new int[]{R.id.text1}) {
            @Override

            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView ev = view.findViewById(R.id.icon);
                ev.setImageResource(menuIconArray[position]);

                return view;
            }
        };
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    Intent i = new Intent(MainActivity.this, PreferencesActivity.class);
                    startActivity(i);
                }

                if(position == 1){
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Information");
                    alertDialog.setMessage("This app is created for students by a student who is in no way associated with Stenden university or Xedule. If you have any questions feel free to contact me\n\n© Luuk Esselbrugge 2018\n\nYour Android Version: "+Build.VERSION.RELEASE);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }

            }
        });


        TextView Name = findViewById(R.id.sidebarName);
        TextView Email = findViewById(R.id.sidebarEmail);
        TextView Letters = findViewById(R.id.sidebarLetters);

        SharedPreferences SP = getApplicationContext().getSharedPreferences("Cookies", MODE_PRIVATE);
        String email = SP.getString("Email","");

        if(!email.equals("")) {
            Email.setText(email);

            String[] tmp = email.split("\\@");
            String[] fn = tmp[0].split("\\.");
            Name.setText("");
            for (String s : fn) {
                Name.setText(Name.getText() + s.substring(0, 1).toUpperCase() + s.substring(1) + " ");
            }

            Letters.setText(fn[0].substring(0, 1).toUpperCase() + fn[1].substring(0, 1).toUpperCase());
        }else{
            Name.setText("Offline Mode");
            Email.setText("Not logged in");
            Letters.setText("OM");
        }

        }

    private void addFavorites() {
        final ListView favlist = (ListView)findViewById(R.id.FavoriteList);
        List<Map<String, String>> CurrentFavo = new ArrayList<>();

        try {
            File file = new File(getDir("data", MODE_PRIVATE), "favorites");
            ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
            CurrentFavo = (List<Map<String, String>>)inputstream.readObject();
            inputstream.close();
        }catch (Exception e){

        }

        final List<Map<String, String>> data = CurrentFavo;
        Map<String, String> additem = new HashMap<>(2);

        if(compareMode){
            additem.put("Name","Cancel Compare");
        }else{
            additem.put("Name","Compare");
        }
        additem.put("Type","5");
        data.add(additem);

        additem = new HashMap<>(2);
        additem.put("Name","Add schedule");
        additem.put("Type","4");
        data.add(additem);

        final SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.list_sidebar,
                new String[]{"Name"},
                new int[]{R.id.text1}) {
            @Override

            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView ev = view.findViewById(R.id.icon);
                String colors[] = new String[]{"#b9d6b9","#d6b9b9","#b9d6d3","#b9b9d6","#d6d6b9","#d6c7b9","#b9d6cb","#c1b9d6","#80d6a9","#808ad6","#d68080","#cbd680"};

                if(!compareMode) {
                    if (data.get(position).get("Type").equals("1")) {
                        ev.setImageResource(R.drawable.ic_group_black_24dp);
                    }
                    if (data.get(position).get("Type").equals("2")) {
                        ev.setImageResource(R.drawable.ic_location_sidebar);
                    }
                    if (data.get(position).get("Type").equals("3")) {
                        ev.setImageResource(R.drawable.ic_person_black_24dp);
                    }
                }else{
                    if(CurrentIDs.contains(data.get(position).get("ID"))){
                        ev.setImageResource(R.drawable.ic_check_circle_black_24dp);
                        try {
                            view.setBackgroundColor(Color.parseColor(colors[CurrentIDs.indexOf(data.get(position).get("ID"))]));
                        }catch (Exception e){
                            Log.e("erro",e.toString());
                        }
                    }else {
                        ev.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp);
                    }
                }

                if(data.get(position).get("Type").equals("4")) {
                    ev.setImageResource(R.drawable.ic_add_circle_black_24dp);
                }
                if(data.get(position).get("Type").equals("5")) {
                    if(compareMode){
                        ev.setImageResource(R.drawable.ic_cancel_sidebar);
                    }else{
                        ev.setImageResource(R.drawable.ic_compare_arrows_black_24dp);
                    }
                }

                return view;
            }
        };
        favlist.setAdapter(adapter);
        favlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(data.get(position).get("Type").equals("5")) {
                    if(compareMode){
                        compareMode = false;
                        if(CurrentIDs.size() > 0){
                            String tmp = CurrentIDs.get(0);
                            CurrentIDs.clear();
                            CurrentIDs.add(tmp);
                        }
                    }else{
                        compareMode = true;
                    }
                    addFavorites();
                    return;
                }

                if(data.get(position).get("Type").equals("4")) {
                    Intent myIntent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivityForResult(myIntent, 1);
                }else{
                    String CurrentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    if(CurrentIDs.size() == 0){
                        CurrentIDs.add(data.get(position).get("ID"));
                    }else{
                        if(compareMode){
                            if(CurrentIDs.contains(data.get(position).get("ID"))){
                                CurrentIDs.remove(data.get(position).get("ID"));
                            }else {
                                CurrentIDs.add(data.get(position).get("ID"));
                            }
                        }else {
                            CurrentIDs.set(0, data.get(position).get("ID"));
                        }
                    }

                    getSchedule(CurrentDate,CurrentIDs);
                    if(!compareMode) {
                        mDrawerLayout.closeDrawers();
                        getSupportActionBar().setTitle(data.get(position).get("Name"));
                    }else{
                        getSupportActionBar().setTitle("Compare");
                        addFavorites();
                    }
                }

            }
        });

        favlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, final View arg1, final int pos, long id) {

                // Set up the input
                final EditText input = new EditText(MainActivity.this);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_NEUTRAL:
                                //Yes button clicked
                                try {
                                    List<Map<String, String>> newfav = new ArrayList<>();
                                    File file = new File(getDir("data", MODE_PRIVATE), "favorites");
                                    ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
                                    List<Map<String, String>> CurrentFavo = (List<Map<String, String>>)inputstream.readObject();
                                    inputstream.close();

                                    for (int i = 0; i < CurrentFavo.size(); i++) {
                                        String id = CurrentFavo.get(i).get("ID");
                                        if (!id.equals(data.get(pos).get("ID"))) {
                                            newfav.add(data.get(i));
                                        }
                                    }
                                    ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                                    outputStream.writeObject(newfav);
                                    outputStream.flush();
                                    outputStream.close();

                                    addFavorites();
                                }catch (Exception e){
                                    Log.e("erro",e.toString());
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;

                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                try {
                                    File file = new File(getDir("data", MODE_PRIVATE), "favorites");
                                    ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
                                    List<Map<String, String>> CurrentFavo = (List<Map<String, String>>)inputstream.readObject();
                                    inputstream.close();

                                    for (int i = 0; i < CurrentFavo.size(); i++) {
                                        String id = CurrentFavo.get(i).get("ID");
                                        if (id.equals(data.get(pos).get("ID"))) {
                                            CurrentFavo.get(i).put("Name",input.getText().toString());
                                        }
                                    }
                                    ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                                    outputStream.writeObject(CurrentFavo);
                                    outputStream.flush();
                                    outputStream.close();

                                    addFavorites();
                                }catch (Exception e){
                                    Log.e("ERROR",e.toString());
                                }
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Edit " + data.get(pos).get("Name"));

                input.setText(data.get(pos).get("Name"));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Save", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener).setNeutralButton("Delete", dialogClickListener).show();

                return true;
            }
        });


    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerList.bringToFront();
                mDrawerLayout.requestLayout();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem weeknum = menu.findItem(R.id.menu_weeknum);
        final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        boolean weekarrows = SP.getBoolean("weekArrows",true);

        if(!weekarrows){
            MenuItem arrowl = menu.findItem(R.id.action_weekback);
            MenuItem arrowr = menu.findItem(R.id.action_weekforward);
            arrowl.setVisible(false);
            arrowr.setVisible(false);
            weeknum.setVisible(false);
        }

        try {
            weeknum.setTitle(CurrentWeekNumber);
        }catch (Exception e){
        Log.e("Error",e.toString());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        final  SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        if (id == R.id.action_date) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);

                    getSchedule(dateFormatter.format(newDate.getTime()),CurrentIDs);
                }

            }, pickedDate.get(Calendar.YEAR), pickedDate.get(Calendar.MONTH), pickedDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();

            return true;
        }

        if(id == R.id.action_weekforward){
            pickedDate.add(Calendar.WEEK_OF_YEAR, 1);
            getSchedule(dateFormatter.format(pickedDate.getTime()),CurrentIDs);

            return true;
        }

        if(id == R.id.action_weekback){
            pickedDate.add(Calendar.WEEK_OF_YEAR, -1);
            getSchedule(dateFormatter.format(pickedDate.getTime()),CurrentIDs);

            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return true;
    }

    public void getSchedule(String datein,final ArrayList<String> ids) {

        View C = findViewById(R.id.content);
        ViewGroup parent1 = (ViewGroup) C.getParent();
        int index = parent1.indexOfChild(C);
        parent1.removeView(C);
        C = getLayoutInflater().inflate(R.layout.sidebar_loading, parent1, false);
        parent1.addView(C, index);

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(datein);

            Calendar c = Calendar.getInstance();
            c.setTime(date);
            pickedDate = c;

            int currentDayNum = c.get(Calendar.DAY_OF_WEEK) - 1;

            if(currentDayNum == 6){
                c.add(Calendar.DATE, 2);  // number of days to add
                date = c.getTime();
            }

            if(currentDayNum == 7){
                c.add(Calendar.DATE, 1);  // number of days to add
                date = c.getTime();
            }

            final String weekNum = new SimpleDateFormat("w").format(date);
            CurrentWeekNumber = weekNum;
            final String year = new SimpleDateFormat("y").format(date);
            final String datetime = new SimpleDateFormat("yyyy-MM-dd").format(date);

         new Thread() {
            public void run() {
                final ServerConnection con = new ServerConnection();
                final JSONArray groups = con.getGroups(getApplicationContext());
                final JSONArray teachers = con.getTeachers(getApplicationContext());
                final JSONArray rooms = con.getRooms(getApplicationContext());
                ArrayList<String> schoolIDs = new ArrayList<String>();

                try {
                    if(groups.length() == 0){
                        Intent stenden = new Intent(MainActivity.this, StendenLogin.class);
                        startActivityForResult(stenden, 2);
                    }
                }catch (Exception e){
                    Log.e("d",e.toString());
                }

                for (String id: ids) {
                    for (int i = 0; i < groups.length(); i++) {
                        try {
                            JSONObject row = groups.getJSONObject(i);
                            if(row.get("id").equals(id)){
                                JSONArray a = (JSONArray)row.get("orus");
                                schoolIDs.add(a.get(0).toString());
                            }
                        } catch (Exception e) {
                            Log.d("Error",e.toString());
                        }
                    }
                }

                for (String id: ids) {
                    for (int i = 0; i < teachers.length(); i++) {
                        try {
                            JSONObject row = teachers.getJSONObject(i);
                            if(row.get("id").equals(id)){
                                JSONArray a = (JSONArray)row.get("orus");
                                schoolIDs.add(a.get(a.length()-1).toString());
                            }
                        } catch (Exception e) {
                            Log.d("Error",e.toString());
                        }
                    }
                }

                for (String id: ids) {
                    for (int i = 0; i < rooms.length(); i++) {
                        try {
                            JSONObject row = rooms.getJSONObject(i);
                            if(row.get("id").equals(id)){
                                JSONArray a = (JSONArray)row.get("orus");
                                schoolIDs.add(a.get(0).toString());
                            }
                        } catch (Exception e) {
                            Log.d("Error",e.toString());
                        }
                    }
                }


                final JSONArray data = con.getSchedule(getApplicationContext(),weekNum,year,ids,schoolIDs);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        addSchedule(data,rooms,groups,teachers,datetime);
                    }
                });
            }
        }.start();

        invalidateOptionsMenu(); // Voor weeknummer in actionbar


        }catch (Exception e){
            Log.e("data",e.toString());
        }

    }

    public void addSchedule(JSONArray json, JSONArray rooms, JSONArray Groups, JSONArray Teachers, final String datetime){

        List<Map<String, String>> data = new ArrayList<>();

        json = sortJsonArray(json,"iStart");
        Date lastEnd = new Date();
        int currentDayNum = 1;

        View C = findViewById(R.id.content);
        ViewGroup parent1 = (ViewGroup) C.getParent();
        int index = parent1.indexOfChild(C);
        parent1.removeView(C);
        C = getLayoutInflater().inflate(R.layout.sidebar_schedule, parent1, false);
        parent1.addView(C, index);

        try {
            Date SelectedDate = new SimpleDateFormat("yyyy-MM-dd").parse(datetime);
            Calendar c = Calendar.getInstance();
            c.setTime(SelectedDate);
            currentDayNum = c.get(Calendar.DAY_OF_WEEK) - 1;
        }catch (Exception e){
            Log.e("erro",e.toString());
        }

        Map<String, String> RoomsList = new HashMap<>();
        for (int i = 0; i < rooms.length(); i++) {
            try {
                JSONObject row = rooms.getJSONObject(i);
                RoomsList.put(row.getString("id"), row.getString("code"));
            } catch (Exception e) {
                Log.e("erro",e.toString());
            }
        }

        Map<String, String> GroupList = new HashMap<>();
        for (int i = 0; i < Groups.length(); i++) {
            try {
                JSONObject row = Groups.getJSONObject(i);
                GroupList.put(row.getString("id"), row.getString("code"));
            } catch (Exception e) {
                Log.e("erro",e.toString());
            }
        }

        Map<String, String> TeacherList = new HashMap<>();
        for (int i = 0; i < Teachers.length(); i++) {
            try {
                JSONObject row = Teachers.getJSONObject(i);
                TeacherList.put(row.getString("id"), row.getString("code"));
            } catch (Exception e) {
                Log.e("erro",e.toString());
            }
        }

        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject row = json.getJSONObject(i);
                Map<String, String> item = new HashMap<>(2);

                Date Startdate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(row.getString("iStart"));
                Date Enddate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(row.getString("iEnd"));

                    long diff =  Startdate.getTime() - lastEnd.getTime();
                    int minutes = (int) (diff / (1000 * 60));
                    int hours = minutes / 60;
                    int minutesextra = minutes % 60;

                    if(minutes > 15){
                        if(minutes > 60){
                            item.put("WaitTime",hours + " hours, " + minutesextra +" minutes");
                        }else{
                            item.put("WaitTime",minutes + " minutes");
                        }
                    }
                    item.put("Name", row.getString("name"));
                    item.put("Time", new SimpleDateFormat("HH:mm").format(Startdate) + " - " + new SimpleDateFormat("HH:mm").format(Enddate));
                    Calendar cl = Calendar.getInstance();
                    cl.setTime(Startdate); // yourdate is an object of type Date
                    item.put("DayNumber",cl.get(Calendar.DAY_OF_WEEK) - 1 +"");
                    item.put("iStart",row.getString("iStart"));
                    item.put("iEnd",row.getString("iEnd"));
                    item.put("attention",row.getString("attention"));
                    item.put("ID",row.getString("id"));
                    item.put("ComparePos",row.getString("ComparePos"));
                    item.put("CompareMode",compareMode.toString());

                    String LocationText = "";
                    String Divider = "";
                    JSONArray atts = row.getJSONArray("atts"); // Wat een fucking garbage systeem met alle ids door elkaar
                    for (int c = 0; c < atts.length(); c++) {
                        String RoomName = RoomsList.get(atts.getString(c));
                        if(RoomName != null) {
                            LocationText += Divider + RoomName;
                            Divider = ", ";
                        }
                    }
                    item.put("Location", LocationText);

                    Divider = "";
                    String GroupText = "";
                    for (int c = 0; c < atts.length(); c++) {
                        String GroupName = GroupList.get(atts.getString(c));
                        if(GroupName != null) {
                            GroupText += Divider + GroupName;
                            Divider = ", ";
                        }
                    }
                    item.put("Groups", GroupText);

                    Divider = "";
                    String TeacherText = "";
                    for (int c = 0; c < atts.length(); c++) {
                        String TeacherName = TeacherList.get(atts.getString(c));
                        if(TeacherName != null) {
                            TeacherText += Divider + TeacherName;
                            Divider = " ";
                        }
                    }
                    item.put("Teachers", TeacherText);

                    data.add(item);
                    lastEnd = Enddate;

            }catch (Exception e){
                Log.d("error",e.toString());

            }
        }
        if(data.size() == 0){
            parent1.removeView(C);
            C = getLayoutInflater().inflate(R.layout.sidebar_error_nodata, parent1, false);
            parent1.addView(C, index);
        }else {
            // Get the ViewPager and set it's PagerAdapter so that it can display items
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
            viewPager.setOffscreenPageLimit(5);
            viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), MainActivity.this, data));
            viewPager.setCurrentItem(currentDayNum - 1);

            // Give the TabLayout the ViewPager
            tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
            tabLayout.setupWithViewPager(viewPager);
        }

        offlineModeCheck();

    }

    public static JSONArray sortJsonArray(JSONArray array, final String name) {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
        }catch (Exception e){
            Log.e("erro",e.toString());
        }
        Collections.sort(jsons, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                try {
                    String lid = lhs.getString(name);
                    String rid = rhs.getString(name);
                    // Here you could parse string id to integer and then compare.
                    return lid.compareTo(rid);
                }catch (Exception e){
                    Log.e("erro",e.toString());
                }
                return 0;
            }
        });
        return new JSONArray(jsons);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                addFavorites();

                List<Map<String, String>> CurrentFavo = new ArrayList<>();

                try {
                    File file = new File(getDir("data", MODE_PRIVATE), "favorites");
                    ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
                    CurrentFavo = (List<Map<String, String>>)inputstream.readObject();
                    inputstream.close();

                    String CurrentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    CurrentIDs.clear();
                    CurrentIDs.add(0,CurrentFavo.get(CurrentFavo.size() - 1).get("ID"));
                    getSchedule(CurrentDate,CurrentIDs);
                    getSupportActionBar().setTitle(CurrentFavo.get(CurrentFavo.size() - 1).get("Name"));

                    addDrawerItems();

                }catch (Exception e){

                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

        if (requestCode == 2) {
            checkLogin();
        }
    }

    public void scheduleNotification(String title, String content, int id, long delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra("Title", title);
        notificationIntent.putExtra("Content", content);
        notificationIntent.putExtra("id",id+"");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);

        Map<String, Map<String, String>> CurrentSchedule = new HashMap<>();
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "schedule");
            ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
            CurrentSchedule = (Map<String, Map<String, String>>)inputstream.readObject();
            inputstream.close();
        }
        catch(Exception e){
            Log.e("erro",e.toString());
            }

            try {
            Map<String, String> item = new HashMap<>();
            item.put("Title", title);
            item.put("Content", content);
            item.put("ID", id+"");
            item.put("DelayFinal", futureInMillis+"");

            CurrentSchedule.put(id+"",item);
            File file = new File(getDir("data", MODE_PRIVATE), "schedule");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(CurrentSchedule);
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            Log.d("ERORR",e.toString());
        }

    }

    public void cancelNotification(int id){
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        try {
            File file = new File(getDir("data", MODE_PRIVATE), "schedule");
            ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
            Map<String,Map<String, String>> CurrentSchedule = (Map<String,Map<String, String>>)inputstream.readObject();
            inputstream.close();

            CurrentSchedule.remove(id+"");

            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(CurrentSchedule);
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            Log.e("erro",e.toString());
        }
    }

    public boolean checkNotificationExists(String id){
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "schedule");
            ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(file));
            Map<String,Map<String, String>> CurrentSchedule = (Map<String,Map<String, String>>)inputstream.readObject();
            inputstream.close();

            if(CurrentSchedule.get(id) != null){
                return true;
            }

        }catch (Exception e){
            return false;
        }
        return false;
    }

    public void offlineModeClick(View v){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Offline Mode");
        alertDialog.setMessage("You are seeing this message because you have no internet connection or your login information has expired\n\n" +
                "This schedule has been loaded from your last session. It might no longer be up to date\n\n" +
                "In order to view the latest schedule, please login or connect to the internet");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Login",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent stenden = new Intent(MainActivity.this, StendenLogin.class);
                        startActivityForResult(stenden, 2);
                    }
                });
        alertDialog.show();
    }
}


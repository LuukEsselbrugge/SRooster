package com.stenden.esselbrugge.stendenrooster;

/**
 * Created by luuk on 21-9-17.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TabFragment extends Fragment {


    private static List<Map<String, String>> ddata;
    private int page;
    private static Context ctx;


    public static TabFragment newInstance(int page, Context context,  List<Map<String, String>> data) {
        ddata = data;
        ctx = context;
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("num", 0);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;

        view = inflater.inflate(R.layout.fragment_tab_ma, container, false);
        ListView list = view.findViewById(R.id.ScheduleList_ma);
        page++;

        final List<Map<String, String>> data1 = new ArrayList<>();
        try {
            if (ddata.size() > 0) { //Fixed rare bug met forceclosed nadat app tijdje aanstaat
                for (int i = 0; i < ddata.size(); i++) {
                    String day = ddata.get(i).get("DayNumber");
                    if (Integer.toString(page).equals(day)) {
                        data1.add(ddata.get(i));
                    }
                }
            }
        }catch (NullPointerException e){
            Log.e("erro",e.toString());
            return view;
        }

        ListAdapter adapter = new SimpleAdapter(getContext(), data1,
                R.layout.list_rooster,
                new String[]{"Name","Time","Location","WaitTime","Groups","Teachers"},
                new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5, R.id.text6}) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                    View view = super.getView(position, convertView, parent);
                    TextView WaitTime = view.findViewById(R.id.text4);
                    TextView RoomText = view.findViewById(R.id.text3);
                    LinearLayout itemDetails = view.findViewById(R.id.itemDetails);
                    TextView RoomText2 = view.findViewById(R.id.text3_2);
                    final TextView Reminder = view.findViewById(R.id.reminder);
                    TextView TeacherText = view.findViewById(R.id.text6);
                    LinearLayout itemSchedule = view.findViewById(R.id.itemSchedule);
                    try {
                        itemSchedule.setBackground(getResources().getDrawable(R.drawable.rooster_bg));
                    }catch (Exception e){
                        return view;
                    }

                    final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getContext());
                    boolean autoexpand = SP.getBoolean("autoExpand", false);

                    if (autoexpand) {
                        itemDetails.setVisibility(View.VISIBLE);
                        RoomText.setVisibility(View.GONE);
                    }

                    LinearLayout WaitTimeBox = view.findViewById(R.id.itemTime);

                    RoomText2.setText(RoomText.getText());
                    if (RoomText.getText() == "") {
                        RoomText.setVisibility(View.GONE);
                        RoomText2.setText("No Room Specified");
                    }
                    if (TeacherText.getText() == "") {
                        TeacherText.setText("No Teacher Specified");
                    }
                    if (WaitTime.getText() == "" || position == 0 || data1.get(position).get("CompareMode").equals("true")) {
                        WaitTimeBox.setVisibility(View.GONE);
                    }

                    MainActivity main = (MainActivity) ctx;

                    if (main.checkNotificationExists(data1.get(position).get("ID"))) {
                        Reminder.setCompoundDrawablesWithIntrinsicBounds(R.drawable.alarmactive, 0, 0, 0);
                    }

                    Reminder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity main = (MainActivity) ctx;

                            if (main.checkNotificationExists(data1.get(position).get("ID"))) {
                                Reminder.setCompoundDrawablesWithIntrinsicBounds(R.drawable.alarmoffinline, 0, 0, 0);
                                main.cancelNotification(Integer.parseInt(data1.get(position).get("ID")));
                            } else {
                                try {
                                    Date time1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(data1.get(position).get("iStart"));
                                    //Date time1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2017-09-30T16:44:00");

                                    Calendar calendar1 = Calendar.getInstance();
                                    calendar1.setTime(time1);
                                    calendar1.add(Calendar.DATE, 1);

                                    Calendar calendar2 = Calendar.getInstance();
                                    calendar2.add(Calendar.DATE, 1);

                                    long milUntil = calendar1.getTimeInMillis() - calendar2.getTimeInMillis();
                                    milUntil -= Long.parseLong(SP.getString("reminderTime", "900000"));
                                    int minutes = (int) ((Long.parseLong(SP.getString("reminderTime", "900000")) / (1000 * 60)) % 60);
                                    int hours = (int) ((Long.parseLong(SP.getString("reminderTime", "900000")) / (1000 * 60 * 60)) % 24);
                                    String time = minutes + " Minutes";

                                    if (hours > 0) {
                                        time = hours + " Hour";
                                    }
                                    Reminder.setCompoundDrawablesWithIntrinsicBounds(R.drawable.alarmactive, 0, 0, 0);
                                    main.scheduleNotification(data1.get(position).get("Name"), data1.get(position).get("Location")
                                            + " In " + time, Integer.parseInt(data1.get(position).get("ID")), milUntil);
                                } catch (Exception e) {
                                    Log.d("", e.toString());
                                }
                            }

                        }
                    });

                    try {
                        Date time1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(data1.get(position).get("iStart"));
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTime(time1);
                        calendar1.add(Calendar.DATE, 1);

                        Date time2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(data1.get(position).get("iEnd"));
                        Calendar calendar2 = Calendar.getInstance();
                        calendar2.setTime(time2);
                        calendar2.add(Calendar.DATE, 1);

                        Calendar calendar3 = Calendar.getInstance();
                        calendar3.add(Calendar.DATE, 1);
                        Date x = calendar3.getTime();

                        Drawable bg = ResourcesCompat.getDrawable(getResources(), R.drawable.rooster_bg, null);
                        bg.setTint(Color.parseColor("#e4e4e4"));
                        itemSchedule.setBackground(bg);

                        if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                            itemSchedule.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.rooster_bg_current, null));
                        }

                        if (data1.get(position).get("CompareMode").equals("true")) {
                            String colors[] = new String[]{"#b9d6b9", "#d6b9b9", "#b9d6d3", "#b9b9d6", "#d6d6b9", "#d6c7b9", "#b9d6cb", "#c1b9d6", "#80d6a9", "#808ad6", "#d68080", "#cbd680"};
                            int ComparePos = Integer.parseInt(data1.get(position).get("ComparePos"));

                            int duplicateAmount = 0;
                            for (int i = 0; i < data1.size(); i++) {
                                String id = data1.get(i).get("ID");
                                if (id.equals(data1.get(position).get("ID"))) {
                                    duplicateAmount++;
                                }
                            }

                            try {
                                if (duplicateAmount == 1) {
                                    bg = ResourcesCompat.getDrawable(getResources(), R.drawable.rooster_bg_current, null);
                                    bg.setTint(Color.parseColor(colors[ComparePos]));
                                    itemSchedule.setBackground(bg);
                                } else {
                                    if (ComparePos != 0) {
                                        return inflater.inflate(R.layout.blank_layout, parent, false);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("erro",e.toString());
                            }

                        }

                        if(data1.get(position).get("attention").equals("Wijziging")){
                            bg = ResourcesCompat.getDrawable(getResources(), R.drawable.rooster_bg, null);
                            bg.setTint(Color.parseColor("#d69a9a"));
                            itemSchedule.setBackground(bg);
                        }

                        if (calendar3.after(calendar1)) {
                            Reminder.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return view;
                }

            };
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()

            {
                @Override
                public void onItemClick (AdapterView < ? > parent, View convertView,int position,
                long id){
                LinearLayout itemDetails = convertView.findViewById(R.id.itemDetails);
                TextView Location = convertView.findViewById(R.id.text3);
                if (itemDetails.getVisibility() == View.GONE) {
                    itemDetails.setVisibility(View.VISIBLE);
                    Location.setVisibility(View.GONE);
                } else {
                    itemDetails.setVisibility(View.GONE);
                    if (!Location.getText().equals("")) {
                        Location.setVisibility(View.VISIBLE);
                    }
                }

            }
            });

        return view;

        }

    }

package com.stenden.esselbrugge.stendenrooster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by luuk on 19-9-17.
 */

public class ServerConnection {

    private String user_agent = "SRooster Android";
    private String master_server = "https://sa-nhlstenden.xedule.nl/api/";
    private CookieManager cookieManager;

    public ServerConnection(){
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    public String httpGET(String file, Context context){

        StringBuffer data = new StringBuffer("");

        try{
            SharedPreferences SP = context.getSharedPreferences("Cookies", MODE_PRIVATE);
            String cookie = SP.getString("Cookie","");

            URL url = new URL(master_server + file);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", user_agent);
            connection.setRequestProperty("Cookie", cookie);
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                data.append(line);
            }

        } catch (IOException e) {
            // writing exception to log
            Log.d("Error",e.toString());
            Log.d("Using Cache",file);

            //Probeer eerst login
            SharedPreferences SP1 = context.getSharedPreferences("Cookies", MODE_PRIVATE);
            String em = SP1.getString("Email","");
            String pw = SP1.getString("Password","");

            if(!em.equals("") && !pw.equals("")) {
                final String koekjes = this.Login(em, pw, context);
                if (!koekjes.equals("Error") && !koekjes.equals("")) {
                    SP1.edit().putString("Cookie", koekjes).commit();
                    Log.d("Error", "New session auto created");
                    return httpGET(file, context);
                }
            }

            //Probeer te laden uit cache
            SharedPreferences SP = context.getSharedPreferences("WebCache", MODE_PRIVATE);
            return SP.getString(file,"");
        }

        try{
            final SharedPreferences.Editor SP = context.getSharedPreferences("WebCache", MODE_PRIVATE).edit();
            SP.putString(file, data.toString());
            SP.apply();

        }catch (Exception e){

        }

        return data.toString();
    }

    private String httpPOST(String urlIn, String urlParameters, Context context){

        StringBuffer data = new StringBuffer("");

        byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
        int    postDataLength = postData.length;
        try{

            URL url = new URL(urlIn);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", user_agent);
            connection.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            connection.setRequestProperty( "Accept", "*/*");
            try( DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
                wr.write( postData );
            }
            //Like waarom in de fuck is dit een ding

            //if(connection.getResponseCode() == 307){
            try {
                String redirect = connection.getHeaderField("Location");
                Log.e("Redirect Detected", redirect);
            }catch (Exception e){

            }
            //}

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                data.append(line);
            }

        } catch (IOException e) {
            // writing exception to log
            Log.d("Error",e.toString());
        }

        return data.toString();
    }

    public String httpGETNoCache(String file, Context context){

        StringBuffer data = new StringBuffer("");

        try{
            SharedPreferences SP = context.getSharedPreferences("Cookies", MODE_PRIVATE);
            String cookie = SP.getString("Cookie","");

            URL url = new URL(master_server + file);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", user_agent);
            connection.setRequestProperty("Cookie", cookie);
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                data.append(line);
            }

        } catch (IOException e) {
            // writing exception to log
            Log.d("Error",e.toString());
            Log.d("Using Cache",file);
        }

        return data.toString();
    }

    public JSONArray getSchedule(Context context, String weekNum, String year, ArrayList<String> ids, ArrayList<String>schoolIDs){

        String rawdata;
        JSONArray finalData = new JSONArray();

        int count = 0;
        int maxTries = 5;
        while(true) {

        try {
            int x = 0;
            for (String id: ids) {
                rawdata = httpGET("schedule/?ids%5B0%5D=" + schoolIDs.get(x) + "_" + year + "_" + weekNum + "_" + id, context);
                Log.e("data",rawdata.toString());
                JSONArray data = new JSONArray(rawdata).getJSONObject(0).getJSONArray("apps");

                for (int i = 0; i < data.length(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    item.put("ComparePos",x+"");

                    finalData.put(item);
                }
                x++;
            }

            return finalData;

        }catch (Exception e){
            Log.d("ShitWentWrondddddddg",e.toString());
            year = (Integer.parseInt(year)-1) + "";
            if (++count == maxTries) return new JSONArray(); // Dit geweldige fucking stukje code komt omdat ze het jaar 2017 gebruiken voor 2018 items
        }

        }
    }

    public JSONArray checkMode(Context context){
        String data;
        try {
            data = httpGETNoCache("group", context);

            JSONArray jObject = new JSONArray(data);

            return jObject;
        }catch (Exception e){
            Log.d("ShitWentWrong",e.toString());
            return new JSONArray();
        }
    }


    public JSONArray getGroups(Context context){
        String data;
        try {
            data = httpGET("group", context);

            JSONArray jObject = new JSONArray(data);

            return jObject;
        }catch (Exception e){
            Log.d("ShitWentWrong",e.toString());
            return new JSONArray();
        }
    }
    public JSONArray getRooms(Context context){
        String data;
        try {
            data = httpGET("facility", context);

            JSONArray jObject = new JSONArray(data);

            return jObject;
        }catch (Exception e){
            Log.d("ShitWentWrong",e.toString());
            return new JSONArray();
        }
    }

    public JSONArray getTeachers(Context context){
        String data;
        try {
            data = httpGET("docent", context);

            JSONArray jObject = new JSONArray(data);

            return jObject;
        }catch (Exception e){
            Log.d("ShitWentWrong",e.toString());
            return new JSONArray();
        }
    }

    //Als Java code aids kan hebben dan komt deze methode in de buurt er is geen andere manier zonder API
    public String Login(String Username, String Password, Context context){

        StringBuffer data = new StringBuffer("");
        String firstpostURL = "";

        try{
            URL url = new URL("https://sa-nhlstenden.xedule.nl/Stenden");

            if(Username.endsWith("@student.nhlstenden.com") || Username.endsWith("@nhlstenden.com")){
                url = new URL("https://sa-nhlstenden.xedule.nl");
            }

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", user_agent);
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                data.append(line);
            }

        } catch (IOException e) {
            // writing exception to log
            Log.d("Error",e.toString());
        }

        Pattern pattern = Pattern.compile("<form id=\"options\"  method=\"post\" action=\"(.*?)\">");
        Matcher matcher = pattern.matcher(data);
        while (matcher.find()) {
            firstpostURL = matcher.group(1);
        }

        Boolean DetailsWrong = true;

        try {
            String SAMLResponse = httpPOST(firstpostURL, "UserName=" + URLEncoder.encode(Username, "UTF-8") + "&Password=" + URLEncoder.encode(Password, "UTF-8") + "&Kmsi=true&AuthMethod=FormsAuthentication", context);

            pattern = Pattern.compile("name=\"SAMLResponse\" value=\"(.*?)\" />");
            matcher = pattern.matcher(SAMLResponse);
            while (matcher.find()) {
                DetailsWrong = false;
                SAMLResponse = matcher.group(1);
            }

            if(DetailsWrong){
                return "Error";
            }

            String surf = httpPOST("https://engine.surfconext.nl:443/authentication/sp/consume-assertion","SAMLResponse="+URLEncoder.encode(SAMLResponse, "UTF-8"),context);

            String surfAuth = surf;
            String RelayState = surf;

            pattern = Pattern.compile("<input type=\"hidden\" name=\"SAMLResponse\" value=\"(.*?)\"");
            matcher = pattern.matcher(surfAuth);
            while (matcher.find()) {
                surfAuth = matcher.group(1);
            }

            pattern = Pattern.compile("<input type=\"hidden\" name=\"RelayState\" value=\"(.*?)\"");
            matcher = pattern.matcher(RelayState);
            while (matcher.find()) {
                RelayState = matcher.group(1);
            }

            RelayState = RelayState.replace("&amp;", "&");

            String Auth2 = httpPOST("https://sa-nhlstenden.xedule.nl/authentication/sso/assertionservice.aspx","SAMLResponse="+URLEncoder.encode(surfAuth, "UTF-8") + "&return=&RelayState="+URLEncoder.encode(RelayState, "UTF-8"),context);

            CookieStore cookieJar =  cookieManager.getCookieStore();
            List<HttpCookie> cookies =
                    cookieJar.getCookies();

            String kutKoekjes = "";
            for (HttpCookie cookie: cookies) {
                kutKoekjes += cookie + "; ";
            }

            return kutKoekjes;

        }catch (Exception e){
            Log.e("Error",e.toString());
        }

        return "";

    }
}

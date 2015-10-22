package club.easley.musiquik;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.internal.view.WindowCallbackWrapper;
import android.test.suitebuilder.annotation.Suppress;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import android.widget.TextView;
import android.widget.Toast;

import com.flyco.animation.ZoomEnter.ZoomInTopEnter;
import com.flyco.animation.ZoomExit.ZoomOutBottomExit;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.droidparts.contract.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


import club.easley.fragments.SearchFragment;

/**
 * Created by measley on 8/8/2015.
 */
public class GetSongListTask extends AsyncTask<String, Void, Void> {

    private static final String TRACKS = "tracks";
    private static final String NAME = "name";
    private static final String ARTIST = "artist";
    private static final String DIRECT = "direct";

    ProgressDialog pDialog;
    SearchFragment searchFragment;
    String query;

    //tracks JSONArray
    JSONArray tracks = null;
    //Hashmap for ListView
    ArrayList<HashMap<String, String>> trackList;
    ListView lv;
    DownloadManager downloadManager;


    private long myDownloadReference;
    private BroadcastReceiver receiverDownloadComplete;
    private BroadcastReceiver receiverNotificationClicked;
    private String searchEngineName = "";


    private HttpClient httpClient;
    private HttpPost httpPost;
    private String q;
Typeface font;

    public GetSongListTask(SearchFragment searchFragment) {
        this.searchFragment = searchFragment;
        trackList = new ArrayList<HashMap<String, String>>();
        downloadManager = (DownloadManager) searchFragment.getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        font = Typeface.createFromAsset(searchFragment.getActivity().getAssets(), "fonts/QuicksandRegular.otf");

    }

    @Override
    protected Void doInBackground(String... params) {


        String searchQuery = params[0];
        q = params[0];

        String jsonStr = "";

        //store search in external DB
        httpClient = new DefaultHttpClient();
        httpPost = new HttpPost("http://heisenberg.musicgold.info/searchlog/index.php/MusicGoldLogger/add_search");
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
        nameValuePair.add(new BasicNameValuePair("query", searchQuery));

        String ipAddress = Utils.getIPAddress(true);
        if (ipAddress.equals("")) ipAddress = Utils.getIPAddress(false);
        nameValuePair.add(new BasicNameValuePair("request_ip", ipAddress));

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        nameValuePair.add(new BasicNameValuePair("searched", dateFormat.format(date)));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("Http Post Response:", response.toString());
        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }


        /*Integer[] servers = new Integer[]{1,2,3,4};

        Collections.shuffle(Arrays.asList(servers));
        int serverId = 0;
        //2 1 3 4
        for(int i=0; i<servers.length; i++){
            if (jsonStr.equals("") || jsonStr.equals("{}") || jsonStr.length() == 13) {
                if(servers[i] == 1){
                    jsonStr = makePostRequestMP3Monkey(searchQuery);
                    serverId = 1;
                    searchEngineName = "mp3monkey";

                } else if (servers[i] == 2){
                    jsonStr = makePostRequestMP3Rehab(searchQuery);
                    serverId = 2;
                    searchEngineName = "mp3rehab";
                } else if (servers[i] == 3){
                    jsonStr = makePostRequestMP3COFE(searchQuery);
                    serverId = 3;
                    searchEngineName = "mp3cofe";
                } else if (servers[i] == 4){
                    jsonStr = makePostRequestTrendingMP3(searchQuery);
                    serverId = 4;
                    searchEngineName = "trendingMP3";
                }

            } else{
                Log.d("server id",String.valueOf(serverId));
                break;
            }

        }*/


        jsonStr = makePostRequestMP3COFE(searchQuery);
        searchEngineName = "mp3cofe";

//TODO: check out emp3world.com which redirects to emp3world.to


        if (jsonStr.equals("{}") || jsonStr.length() == 13) {

            jsonStr = makePostRequestMP3Monkey(searchQuery);
            searchEngineName = "mp3monkey"; //TODO: check on mp3monkey

        }
        if (jsonStr.equals("{}") || jsonStr.length() == 13) {


            jsonStr = makePostRequestTrendingMP3(searchQuery);
            searchEngineName = "trendingMP3"; //TODO: appears down for everyone
        }
        if (jsonStr.equals("{}") || jsonStr.length() == 13) {


            jsonStr = makePostRequestMP3Rehab(searchQuery);
            searchEngineName = "mp3rehab"; //TODO: check on mp3rehab...down for everybody
        }
        if (jsonStr.equals("{}") || jsonStr.length() == 13) {

            Log.d("*****NO RESULTS*****", "No results. Don't attempt Json parse");
            return null;
        }


        Log.d("JSON String Length", "[" + jsonStr.length() + "]");
        Log.d("JSON String", "[" + jsonStr + "]");


        trackList.clear();

        if (jsonStr != null) {
            try {

                JSONObject jsonObj = new JSONObject(jsonStr);

                // Getting JSON Array node
                tracks = jsonObj.getJSONArray(TRACKS);

                // looping through All Contacts
                for (int i = 0; i < tracks.length(); i++) {
                    JSONObject c = tracks.getJSONObject(i);

                    String artist = c.getString(ARTIST);
                    String name = c.getString(NAME);
                    String direct = c.getString(DIRECT);

                    //no need to even worry about songs with long titles
                    if (name.length() > 100) {
                        continue;
                    }

                    // tmp hashmap for single contact
                    HashMap<String, String> track = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    track.put(ARTIST, artist);
                    track.put(NAME, name);
                    track.put(DIRECT, direct);


                    // adding contact to contact list
                    trackList.add(track);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
        }
        return null;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //show progress dialog
        pDialog = new ProgressDialog(searchFragment.getActivity());
        pDialog.setMessage("Searching...");
        pDialog.setCancelable(true);
        pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
        });
        pDialog.show();
    }

    @Override
    protected void onPostExecute(Void result) {


        super.onPostExecute(result);
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }

        if (trackList.size() == 0) {
            Toast.makeText(searchFragment.getActivity(), "No results. Try narrowing your search.", Toast.LENGTH_LONG).show();
        }



        ListAdapter adapter = new SimpleAdapter(
                searchFragment.getActivity().getBaseContext(),
                trackList,
                R.layout.list_item,
                new String[]{NAME, ARTIST, DIRECT},
                new int[]{R.id.name, R.id.artist, R.id.direct}){

            @Override
            public View getView(int pos, View convertView, ViewGroup parent){
                View v = convertView;
                if(v == null){
                    LayoutInflater vi = (LayoutInflater) searchFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.list_item,null);
                }
                TextView tvSongName = (TextView)v.findViewById(R.id.name);
                tvSongName.setText(trackList.get(pos).get(NAME));

                TextView tvArtistName = (TextView)v.findViewById(R.id.artist);
                tvArtistName.setText(trackList.get(pos).get(ARTIST));
                TextView tvDirect = (TextView)v.findViewById(R.id.direct);
                tvDirect.setText(trackList.get(pos).get(DIRECT));

                tvSongName.setTypeface(font, Typeface.BOLD);
                tvArtistName.setTypeface(font, Typeface.BOLD);
                tvDirect.setTypeface(font, Typeface.BOLD);

                return v;
            }
        };

        lv = (ListView) searchFragment.getActivity().findViewById(R.id.json_view);

        lv.setFastScrollEnabled(true);
        lv.setAdapter(null); //clear list
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String name = ((TextView) view.findViewById(R.id.name)).getText().toString().trim();

                final String artist = ((TextView) view.findViewById(R.id.artist)).getText().toString().trim();

                final String direct = ((TextView) view.findViewById(R.id.direct)).getText().toString().trim();

                //remove spaces from name

                String o_name = name.replaceAll(" ", "_").replaceAll("\\(\\d*:\\d*\\)", "").replaceAll(":", "_").toLowerCase().trim();
                String o_artist = artist.replaceAll(" ", "_").toLowerCase().trim();
                final String outFile = o_name + "_" + o_artist + ".mp3";          //_musicbandit.mp3";
                Log.d("outFile", outFile);
                //remove spaces from artist

                //check if File exists before
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, outFile);
                Log.d("FILE TO WRITE", file.toString());
                if (file.exists()) {
                    System.out.println("FILE EXISTS");
                    Toast.makeText(searchFragment.getActivity(), "Song file already exists. Delete current file first.", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d("FILE TO WRITE", file.toString());


                new ProgressDialog.Builder(searchFragment.getActivity())
                        .setTitle("Download Song")
                        .setMessage("Download song: " + name + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //make sure not currently downloading file already


                                        Toast.makeText(searchFragment.getActivity(), "Starting download...", Toast.LENGTH_SHORT).show();

                                        //handle the download....whoo hoo, bitches!!!
                                        Uri uri = Uri.parse(direct);
                                        DownloadManager.Request request = new DownloadManager.Request(uri);


//
                                        //store search in external DB
                                        httpClient = new DefaultHttpClient();
                                        httpPost = new HttpPost("http://heisenberg.musicgold.info/searchlog/index.php/MusicGoldLogger/add_download");
                                        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(6);

                                        String ipAddress = Utils.getIPAddress(true);
                                        if (ipAddress.equals(""))
                                            ipAddress = Utils.getIPAddress(false);

                                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date date = new Date();

//name artist direct
                                        nameValuePair.add(new BasicNameValuePair("query", q));
                                        nameValuePair.add(new BasicNameValuePair("request_ip", ipAddress));
                                        nameValuePair.add(new BasicNameValuePair("artist", artist));
                                        nameValuePair.add(new BasicNameValuePair("song", name));
                                        nameValuePair.add(new BasicNameValuePair("direct_url", direct));
                                        nameValuePair.add(new BasicNameValuePair("downloaded", dateFormat.format(date)));

                                        try {
                                            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));

                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            HttpResponse response = httpClient.execute(httpPost);
                                            // write response to log
                                            Log.d("Http Post Response:", response.toString());
                                        } catch (ClientProtocolException e) {
                                            // Log exception
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            // Log exception
                                            e.printStackTrace();
                                        }

                                        if (searchEngineName.equals("mp3skull")) {
                                            try {
                                                request.addRequestHeader("Host", getDomainName(direct));
                                                request.addRequestHeader("Accept-Language", "en-US,en;q=0.5");
                                                request.addRequestHeader("Accept-Encoding", "gzip, deflate");
                                                request.addRequestHeader("Connection", "keep-alive");
                                                request.addRequestHeader("Accept", "audio/mpeg, audio/MP3, text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                                                request.addRequestHeader("Referer", getDomainName(direct));
                                            } catch (URISyntaxException urise) {
                                                urise.printStackTrace();
                                            }
                                        }

                                        if (searchEngineName.equals("trendingMP3")) {
                                            try {
                                                request.addRequestHeader("Host", getDomainName(direct));
                                                request.addRequestHeader("Accept-Language", "en-US,en;q=0.5");
                                                request.addRequestHeader("Accept-Encoding", "gzip, deflate");
                                                request.addRequestHeader("Connection", "keep-alive");
                                                request.addRequestHeader("Accept", "audio/mpeg, audio/MP3, text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                                                request.addRequestHeader("Referer", getDomainName(direct));
                                            } catch (URISyntaxException urise) {
                                                urise.printStackTrace();
                                            }
                                        }

                                        if (searchEngineName.equals("mp3cofe")) {
                                            request.addRequestHeader("Host", "mp3cofe.com");
                                            //request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0");
                                            //request.addRequestHeader("Accept", "audio/mp3; q=0.2; version=0.5, audio/basic+mp3");
                                            //request.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

                                            request.addRequestHeader("Accept-Language", "en-US,en;q=0.5");
                                            request.addRequestHeader("Accept-Encoding", "gzip, deflate");
                                            request.addRequestHeader("Referer", "http://mp3cofe.com");
                                            request.addRequestHeader("Connection", "keep-alive");
                                            request.addRequestHeader("Connection", "keep-alive");
                                            request.addRequestHeader("Accept", "audio/mpeg, audio/MP3, text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

                                        }

                                        if (searchEngineName.equals("mp3rehab")) {
                                            try {
                                                request.addRequestHeader("Host", getDomainName(direct));
                                                request.addRequestHeader("Accept-Language", "en-US,en;q=0.5");
                                                request.addRequestHeader("Accept-Encoding", "gzip, deflate");
                                                request.addRequestHeader("Connection", "keep-alive");
                                                request.addRequestHeader("Accept", "audio/mpeg, audio/MP3, text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                                                request.addRequestHeader("Referer", getDomainName(direct));
                                            } catch (URISyntaxException urise) {
                                                urise.printStackTrace();
                                            }
                                        }

                                        if (searchEngineName.equals("mp3monkey")) {
                                            try {
                                                request.addRequestHeader("Host", getDomainName(direct));
                                                request.addRequestHeader("Accept-Language", "en-US,en;q=0.8");
                                                request.addRequestHeader("Accept-Encoding", "gzip, deflate, sdch");
                                                request.addRequestHeader("Connection", "keep-alive");
                                                request.addRequestHeader("Accept", "audio/mpeg, audio/MP3, text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                                                request.addRequestHeader("Referer", getDomainName(direct));
                                                request.addRequestHeader("Upgrade-Insecure-Requests", "1");
                                                request.addRequestHeader("Cache-Control", "no-cache");
                                                request.addRequestHeader("Pragma", "no-cache");
                                                request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36");
                                            } catch (URISyntaxException urise) {
                                                urise.printStackTrace();
                                            }
                                        }


//                                        if (direct.contains("mp3zap")) {
//                                            request.addRequestHeader("Host", "mp3zap.com");
//                                            request.addRequestHeader("Accept-Language", "en-US,en;q=0.5");
//                                            request.addRequestHeader("Accept-Encoding", "gzip, deflate");
//                                            request.addRequestHeader("Referer", "http://mp3zap.com");
//                                            request.addRequestHeader("Accept", "audio/mpeg, audio/MP3, text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//                                        }

                                        request.setMimeType("audio/mpeg");
                                        request.setTitle(name);
                                        request.setDescription("Artist: " + artist);
                                        request.allowScanningByMediaScanner();
                                        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


                                        //request.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

                                        if(isExternalStorageWritable()){
                                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, outFile);
                                            //request.setDestinationInExternalFilesDir(MainActivity.this,"mb_music", name + "_" + artist + ".mp3");
                                            myDownloadReference = downloadManager.enqueue(request);
                                            Log.d("myDownloadReference", String.valueOf(myDownloadReference));
                                        }else{
                                            FlycoMenuDialog noWriteableStorageDialog = new FlycoMenuDialog(searchFragment.getActivity(),new ZoomInTopEnter(), new ZoomOutBottomExit(), searchFragment.getActivity().getResources().getString(R.string.no_writeable_storage_title), searchFragment.getActivity().getResources().getString(R.string.no_writeable_storage_content), "Okay");
                                            noWriteableStorageDialog.showMaterialDialog();
                                        }



                                    }
                                }

                        )
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(searchFragment.getActivity(), "Selection cancelled", Toast.LENGTH_SHORT).show();
                                    }
                                }

                        ).

                        show();

                //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
            }
        });
        lv.setAdapter(adapter);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    private String makePostRequest(String queryBitch) {

        HttpClient httpClient = new DefaultHttpClient();

        //HttpPost httpPost = new HttpPost("http://localhost:3000/getsonginfo/");
        HttpPost httpPost = new HttpPost("http://nodejs-musban.rhcloud.com/getsonginfo");
        //Post Data
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
        nameValuePair.add(new BasicNameValuePair("querystring", queryBitch));

        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //making POST request
        try {
            HttpResponse response = httpClient.execute(httpPost);

            //get actual text content
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder builder = new StringBuilder();
            String str = "";
            while ((str = rd.readLine()) != null) {

                builder.append(str);
            }
            String text = builder.toString();
            Log.d("Http Post Response:", text);

            return text;


        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String makePostRequestMP3COFE(String queryMotherFucker) {
        JSONObject completeJSONTracks = new JSONObject();
        JSONObject jsonTrack;
        JSONArray jsonTracks = new JSONArray();

        String referer;
        //http://mp3cofe.com/?m=all+of+the+lights
        String url = "http://mp3cofe.com";


        try {
            Log.d("TRYING TO CONNECT", "MP3COFE.COM");
            referer = url + "/?m=" + URLEncoder.encode(queryMotherFucker, "UTF-8");

            //Log.d("REFERER", referer);

            Document doc = Jsoup.connect(url)
                    .data("m", queryMotherFucker)
                    .header("Host", "mp3cofe.com")
                            //.header("Host", "get.mycounter.ua")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
                            //.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
                    .header("Accept", "image/png,image/*;q=0.8,*/*;q=0.5")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Referer", referer)
                    .header("Connection", "keep-alive")
                    .timeout(5000)
                    .get();

            Elements mp3list = doc.select("div.mp3list");

            Elements dlinks = mp3list.select("li");
            if (dlinks.size() > 0) {
                String directUrl;
                String artist;
                String name;
                for (Element dlink : dlinks) {

                    jsonTrack = new JSONObject();
                    artist = "";
                    name = "";
                    directUrl = "";

                    //get mp3 download url aka direct link
                    directUrl = dlink.select("a.sm2_load").attr("href");
                    artist = dlink.select("b").text();

                    String[] tmp = dlink.text().split(" - ");

                    if (tmp.length == 2) {
                        name = tmp[1];
                        artist = tmp[0];
                    }
                    if (!artist.isEmpty() && !name.isEmpty() && (name.length() < 50) && directUrl.toLowerCase().endsWith(".mp3")) {


                        //add entries to json object
                        try {
                            jsonTrack.put("name", name);
                            jsonTrack.put("artist", artist);
                            jsonTrack.put("direct", directUrl);
                            jsonTracks.put(jsonTrack);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                    }

                }
                try {
                    completeJSONTracks.put("tracks", jsonTracks);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }


        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


        return completeJSONTracks.toString();


    }

    private String makePostRequestFullSongsNet(String queryMotherFucker) {
        String directUrl;
        String artist;
        String name;

        JSONObject completeJSONTracks = new JSONObject();
        JSONObject jsonTrack;
        JSONArray jsonTracks = new JSONArray();
        //http://mp3cofe.com/?m=all+of+the+lights
        String url = "http://fullsongs.net/searchSuggest.php";


        String referer = "http://fullsongs.net";//url +"?m=" + URLEncoder.encode(query, "UTF-8");
        try {

            Document doc = Jsoup.connect(url)
                    .data("txtSearch", URLEncoder.encode(queryMotherFucker, "UTF-8"))
                    .data("cmdSearch", "Search")
                    .data("type", "mp3")
                    .data("dosearch", "dosearch")

                    .header("Host", "www.fullsongs.net")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, sdch")
                    .header("Referer", "http://www.fullsongs.net")
                    .header("Connection", "keep-alive")
                    .header("Pragma", "no-cache")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("Cache-Control", "no-cache")
                    .followRedirects(true)
                    .timeout(10000)
                    .get();


//        File input = new File("C:\\Users\\IBM_ADMIN\\code\\get_started\\src\\group\\easley\\parser\\testhtml.html");
//        Document doc = Jsoup.parse(input, "UTF-8", url);


            Elements tableList = doc.select("table[class=list]");
            if (tableList.size() > 0) {
                for (Element tableContent : tableList) {

                    jsonTrack = new JSONObject();
                    String tableTitle = tableContent.select("div.title").first().text();
                    if (tableTitle.toLowerCase().endsWith(".mp3")) {

                        //remove mp3 from end of title
                        tableTitle = tableTitle.substring(0, tableTitle.length() - 4);


                        //split song from artist " - " artist name directUrl
                        String[] songArtistArray = tableTitle.split(" - ");

                        if (songArtistArray.length < 2) continue;

                        name = songArtistArray[0];
                        artist = songArtistArray[1];

                        //now traverse the bullshit URL for the directUrl

                        //System.out.println(tableTitle);
                        String urlToDirectUrl = tableContent.select("div.title").select("a").attr("href");
                        Document directUrlPage = Jsoup.connect(urlToDirectUrl)
                                .header("Host", "www.fullsongs.net")
                                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36")
                                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                                .header("Accept-Language", "en-US,en;q=0.8")
                                .header("Accept-Encoding", "gzip, deflate, sdch")
                                .header("Referer", "http://www.fullsongs.net")
                                .header("Connection", "keep-alive")
                                .header("Pragma", "no-cache")
                                .header("Upgrade-Insecure-Request", "1")
                                .header("Cache-Control", "no-cache")
                                .timeout(10000)
                                .get();


                        directUrl = directUrlPage.select("div.tools").select("a").first().attr("href");
                        //directUrl = Jsoup.connect(directUrlPage.select("div.tools").select("a").first().attr("href"))
                        //        .ignoreContentType(true)
                        //.followRedirects(true)
                        //        .get().text();

                        if (!artist.isEmpty() && !directUrl.isEmpty() && !name.isEmpty() && (name.length() < 50)) {


                            //add entries to json object
                            try {
                                jsonTrack.put("name", name);
                                jsonTrack.put("artist", artist);
                                jsonTrack.put("direct", directUrl);
                                jsonTracks.put(jsonTrack);

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                            System.out.println(jsonTrack);


                        }
                    }

                }
                try {
                    completeJSONTracks.put("tracks", jsonTracks);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return completeJSONTracks.toString();


    }

    private String makePostRequestMP3Skull(String queryMotherFucker) {
        String directUrl;
        String artist;
        String name;

        JSONObject completeJSONTracks = new JSONObject();
        JSONObject jsonTrack;
        JSONArray jsonTracks = new JSONArray();
        String realUrl = "";
        String fckhValue = "";


        try {


            Document doc = Jsoup.connect("http://mp3skull.com")
                    .header("Host", "mp3skull.com")
                            //.header("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, sdch")

                    .header("Connection", "keep-alive")
                    .header("Pragma", "no-cache")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("Cache-Control", "no-cache")
                    .header("cookie", "__cfduid=d891f4d5b61976900c127d76e9317a7861439534093")
                    .timeout(10000)
                    .userAgent("Chrome")
                    .followRedirects(true).get();


            realUrl = doc.location();
            Log.d("REAL MP3SKULL URL", realUrl);
            doc = Jsoup.connect(realUrl)
                    .header("Host", realUrl)
                    .header(":method", "GET")
                    .header(":scheme", "https")
                    .header(":version", "HTTP/1.1")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, sdch")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36")
                    .followRedirects(true).get();

            Elements es = doc.select("input");

            for (Element e : es) {

                if (e.attr("name").equals("fckh")) {
                    fckhValue = e.attr("value");
                    break;
                }
            }

            //now get list of fucking songs

            String mp3skullUrl = realUrl + "/search_db.php?q=" + URLEncoder.encode(queryMotherFucker, "UTF-8") + "&fckh=" + fckhValue;
            doc = Jsoup.connect(mp3skullUrl)
                    .header("Host", realUrl)
                    .header(":method", "GET")
                    .header(":path", "/search_db.php?q=" + URLEncoder.encode(queryMotherFucker, "UTF-8") + "&fckh=" + fckhValue)
                    .header(":scheme", "https")
                    .header(":version", "HTTP/1.1")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, sdch")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .header("referer", "https://" + realUrl)
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36")
                    .followRedirects(true).get();

            Elements searchResults = doc.select("div.search_results");
            Elements songResults = searchResults.select("[class^=show");
            for (Element el : songResults) {
                jsonTrack = new JSONObject();

                name = el.select("div#right_song").select("b").text();
                artist = "";
                directUrl = el.select("a[rel=nofollow]").attr("href");
                System.out.println(name);
                System.out.println(directUrl);

                if (!directUrl.isEmpty() && !name.isEmpty()) {


                    //add entries to json object
                    try {
                        jsonTrack.put("name", name);
                        jsonTrack.put("artist", artist);
                        jsonTrack.put("direct", directUrl);
                        jsonTracks.put(jsonTrack);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println(jsonTrack);


                }


            }

            try {
                completeJSONTracks.put("tracks", jsonTracks);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }


        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


        return completeJSONTracks.toString();
    }

    private String makePostRequestTrendingMP3(String queryMotherFucker) {
        String directUrl = "";
        String artist;
        String name;

        JSONObject completeJSONTracks = new JSONObject();
        JSONObject jsonTrack;
        JSONArray jsonTracks = new JSONArray();
        //http://mp3cofe.com/?m=all+of+the+lights
        String url = "http://trendingmp3.com/";

        String query = queryMotherFucker;
        String referer = url;

        try {

            Log.d("TRYING TO CONNECT", "TRENDINGMP3.COM");
            Document doc = Jsoup.connect(url)
                    .data("query", query)
                    .header("Host", "trendingmp3.com")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")

                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate, sdch")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                            //.header("Referer", referer)
                    .header("Connection", "keep-alive")
                    .timeout(5000)
                    .get();

            Elements mp3List = doc.select("ul.mp3-list").select("[id^=play]");

            for (Element el : mp3List) {
                jsonTrack = new JSONObject();
                artist = "";
                name = removeFormattingCharacters(el.select("a").attr("data-title"));

                String li = el.select("ul").select("li").get(1).select("a").attr("href");
                System.out.println(name);


                doc = Jsoup.connect(li)

                        .header("Host", "trendingmp3.com")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")

                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, sdch")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Cache-Control", "no-cache")
                        .header("Pragma", "no-cache")
                        .header("Referer", "trendingmp3.com")
                        .header("Connection", "keep-alive")
                        .timeout(10000)
                        .get();

                directUrl = doc.select("div#download_link").select("a").attr("href");
                System.out.println(directUrl);
                if (!name.isEmpty() && (name.length() < 50) && directUrl.toLowerCase().endsWith(".mp3")) {


                    //add entries to json object
                    try {
                        jsonTrack.put("name", name);
                        jsonTrack.put("artist", artist);
                        jsonTrack.put("direct", directUrl);
                        jsonTracks.put(jsonTrack);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                }


            }
            try {
                completeJSONTracks.put("tracks", jsonTracks);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return "{}";
        }

        return completeJSONTracks.toString();

    }

    private String makePostRequestMP3Rehab(String queryMotherFucker) {
        String directUrl = "";
        String artist;
        String name;

        JSONObject completeJSONTracks = new JSONObject();
        JSONObject jsonTrack;
        JSONArray jsonTracks = new JSONArray();

        String url = "http://mp3rehab.com/find.php";

        String query = queryMotherFucker;

        try {
            Log.d("TRYING TO CONNECT", "MP3REHAB.COM");
            Document doc = Jsoup.connect(url)
                    .data("find", query)
                    .data("sub", "Search")
                    .header("Host", getDomainName(url))
                    .header("Origin", getDomainName(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, sdch")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .header("Referer", getDomainName(url))
                    .header("Connection", "keep-alive")
                    .followRedirects(true)
                    .timeout(5000)
                    .post();


            Elements previewElements = doc.select("div#main").select("div#posts").select("div.preview");


            for (Element songElement : previewElements) {

                jsonTrack = new JSONObject();
                artist = "";
                name = songElement.select("h2").text();
                String preUrl = songElement.select("a").attr("href");


                Document downloadSongDoc = Jsoup.connect(preUrl)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Encoding", "gzip, deflate, sdch")
                        .header("Accept-Language", "en-US,en;q=0.8")
                        .header("Cache-Control", "no-cache")
                        .header("Connection", "keep-alive")
                        .header("Host", getDomainName(url))
                        .header("Pragma", "no-cache")
                        .header("Referer", doc.location())
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36")
                        .get();

                directUrl = downloadSongDoc.select("a.filedownload").attr("href");


                if (!name.isEmpty() && (name.length() < 50) && directUrl.toLowerCase().endsWith(".mp3")) {


                    //add entries to json object
                    try {
                        jsonTrack.put("name", name.replaceAll("MP3", "").trim());
                        jsonTrack.put("artist", artist);
                        jsonTrack.put("direct", directUrl);
                        jsonTracks.put(jsonTrack);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                }


            }
        } catch (URISyntaxException uris) {
            uris.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.d("ERRORERROR", "UGH!!!");
            return "{}";
        }
        try {
            completeJSONTracks.put("tracks", jsonTracks);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return completeJSONTracks.toString();
    }


    private String makePostRequestMP3Monkey(String queryMotherFucker) {
        String directUrl = "";
        String artist;
        String name;

        JSONObject completeJSONTracks = new JSONObject();
        JSONObject jsonTrack;
        JSONArray jsonTracks = new JSONArray();

        String url = "http://mp3monkey.net/searchProxy.php";

        String query = queryMotherFucker;

        try {
            Log.d("TRYING TO CONNECT", "MP3MONKEY.COM");
            Document doc = Jsoup.connect(url)
                    .data("search", query)

                    .header("Host", getDomainName(url))
                    .header("Origin", getDomainName(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, sdch")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .header("Referer", getDomainName(url))
                    .header("Connection", "keep-alive")
                    .followRedirects(true)
                    .timeout(5000)
                    .post();


            Elements songElements = doc.select("div.results").select(".toggle");


            for (Element songElement : songElements) {

                jsonTrack = new JSONObject();
                artist = "";
                name = "";

                String[] parts = songElement.select("b").text().split(" - ", 2);

                if (parts.length < 2) {
                    name = songElement.select("b").text();
                } else {
                    artist = parts[0];
                    name = parts[1];
                }

                String preUrl = songElement.select("div.floatRight").select("a[rel=nofollow]").attr("href");
                UrlValidator urlValidator = new UrlValidator();


                if (urlValidator.isValid(preUrl)) {

                    //if url is valid try to get it, if it fails, then continue

                    URL vURL = new URL(preUrl);
                    HttpURLConnection vURL_connection = (HttpURLConnection) vURL.openConnection();
                    try {
                        vURL_connection.connect();

                    } catch (IOException e) {
                        continue;
                    }

                    Document songDoc = Jsoup.connect(preUrl)
                            //.data("dl", "1")
                            .header("Host", getDomainName(preUrl))
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                            .header("Accept-Encoding", "gzip, deflate, sdch")
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36")
                            .followRedirects(true)
                            .timeout(10000)
                            .ignoreHttpErrors(true)
                            .get();

                    directUrl = songDoc.select("#content").select("center").select("a.button.green").attr("href");

                    //System.out.println(directUrl);
                } else {

                    continue;
                }


                if (!name.isEmpty()) {


                    //add entries to json object
                    try {

                        if (urlValidator.isValid(directUrl)) {
                            jsonTrack.put("name", name);
                            jsonTrack.put("artist", artist);
                            jsonTrack.put("direct", directUrl);
                            jsonTracks.put(jsonTrack);
                        } else {
                            continue;
                        }


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                }


            }

            try {
                completeJSONTracks.put("tracks", jsonTracks);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } catch (URISyntaxException uris) {
            uris.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


        return completeJSONTracks.toString();
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static String removeFormattingCharacters(final String toBeEscaped) {
        StringBuffer escapedBuffer = new StringBuffer();
        for (int i = 0; i < toBeEscaped.length(); i++) {
            if ((toBeEscaped.charAt(i) != '\n') && (toBeEscaped.charAt(i) != '\r') && (toBeEscaped.charAt(i) != '\t')) {
                escapedBuffer.append(toBeEscaped.charAt(i));
            }
        }
        String s = escapedBuffer.toString();
        return s;//
        // Strings.replaceSubString(s, "\"", "")
    }

}
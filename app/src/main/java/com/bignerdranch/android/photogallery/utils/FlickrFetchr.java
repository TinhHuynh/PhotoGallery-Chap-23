package com.bignerdranch.android.photogallery.utils;

import android.net.Uri;
import android.util.Log;

import com.bignerdranch.android.photogallery.model.GalleryItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by TINH HUYNH on 5/8/2017.
 */

public class FlickrFetchr {

    // Flickr api key: 50c991816ca69773cd7ba41762744a72
    private static final String TAG = "FlickrFetchr";

    private static final String API_KEY = "50c991816ca69773cd7ba41762744a72";

    private static int sPage = 1;

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems() {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("page", String.valueOf(FlickrFetchr.getPage()))
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            String editedJsonString = editJsonString(jsonString);
            Log.i(TAG, "Edited JSON: " + editedJsonString);
            longInfo(editedJsonString);
            items = parseItems(items, editedJsonString);
        } catch (IOException e) {
            Log.e(TAG, "Failed to receive items", e);
        }
        return items;
    }

    private String editJsonString(String jsonString) throws IOException {
        StringBuilder builder = new StringBuilder(jsonString);
        // create json array
        int index = builder.indexOf("[");
        builder.replace(0, index, "");

        index = builder.indexOf("},\"stat\":\"ok\"}");

        builder.replace(index, builder.toString().length(), "");
        return builder.toString();
    }

    public void longInfo(String str) {
        if (str.length() > 1000) {
            Log.i(TAG, str.substring(0, 1000));
            longInfo(str.substring(1000));
        } else
            Log.i(TAG, str);
    }

    private List<GalleryItem> parseItems(List<GalleryItem> items, String jsonString) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<GalleryItem>>() {
        }.getType();
        items = gson.fromJson(jsonString, listType);
        return items;
    }

    public static int getPage() {
        return sPage;
    }

    public static void setPage(int page) {
        if(page > 10){
            return;
        }
        sPage = page;
    }

    public static void updatePage(){
        int page = FlickrFetchr.getPage();
        FlickrFetchr.setPage(page++);
    }

}



package com.bignerdranch.android.photogallery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by TINH HUYNH on 5/8/2017.
 */

public class GalleryItem {

    @SerializedName("id")
    private String mId;

    @SerializedName("title")
    private String mCaption;

    @SerializedName("url_s")
    private String mUrl;

    public GalleryItem(String caption, String id, String url) {
        mCaption = caption;
        mId = id;
        mUrl = url;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public String toString() {
        return mCaption;
    }
}

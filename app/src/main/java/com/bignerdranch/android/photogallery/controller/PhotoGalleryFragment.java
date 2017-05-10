package com.bignerdranch.android.photogallery.controller;

import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.photogallery.R;
import com.bignerdranch.android.photogallery.model.GalleryItem;
import com.bignerdranch.android.photogallery.utils.FlickrFetchr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TINH HUYNH on 5/8/2017.
 */

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();

    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        fetchItems();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);

        setupAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                    fetchNextItems();
                    appendMorePhotos();
                }
            });
        } else {
            mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
//                    fetchNextItems();
                    appendMorePhotos();
                }
            });

        }

        ViewTreeObserver observer = mRecyclerView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = mRecyclerView.getMeasuredWidth();
                float scalefactor = getResources().getDisplayMetrics().density * 100;
                int noOfColumns = (int) ((int) width / scalefactor);
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), noOfColumns));
            }
        });


        return v;
    }

    private void appendMorePhotos() {
        if (!mRecyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
            new FetchItemsTask().execute(true);
        }
    }

    private void fetchItems() {
        new FetchItemsTask().execute(false);
    }

    private void fetchNextItems() {
        if (!mRecyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
            fetchItems();
        }
    }

    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mTitleTextView.setText(galleryItem.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Boolean, Void, List<GalleryItem>> {
        boolean mMorePhotos;

        @Override
        protected List<GalleryItem> doInBackground(Boolean... params) {
            mMorePhotos = params[0];
            if (mMorePhotos) {
                FlickrFetchr.updatePage();
            }
            Log.i("FlickrFetchr", String.valueOf(mMorePhotos));
            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {

            if (!mMorePhotos) {
                mItems = galleryItems;
                setupAdapter();
            } else {
                mItems.addAll(galleryItems);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

}

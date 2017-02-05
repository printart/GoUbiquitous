package com.example.android.sunshine;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;

/**
 * Created by printArt
 * ref->https://developer.android.com/training/building-wearables.html
 * Incorrect date
 **/

public class WMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private GoogleApiClient mGoogleApiClient;
    private TextView mDateTextView;
    private ImageView mWeatherIconImageView;
    private TextView mWeatherHighTempTextView;
    private TextView mWeatherLowTempTextView;
    private View mView;
    private TextView mNoDataTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wmain);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mDateTextView = (TextView) stub.findViewById(R.id.date_text_view);
                mWeatherHighTempTextView = (TextView) stub.findViewById(R.id.high_temp_text_view);
                mWeatherLowTempTextView = (TextView) stub.findViewById(R.id.low_temp_text_view);
                mWeatherIconImageView = (ImageView) stub.findViewById(R.id.weather_icon_image_view);
                mView = stub.findViewById(R.id.line_view);
                mView.setVisibility(View.INVISIBLE);
                mNoDataTextView = (TextView) stub.findViewById(R.id.no_data_text_view);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String path = event.getDataItem().getUri().getPath();
                if (path.equals("/watchWeatherData")) {
                    mNoDataTextView.setVisibility(View.INVISIBLE);
                    String highTemp = dataMap.getString("high");
                    String lowTemp = dataMap.getString("low");
                    String date = dataMap.getString("date");
                    //String weatherDescription = dataMap.getString("description");
                    Asset asset = dataMap.getAsset("weather_icon");
                    new SetBitmapAsync().execute(asset);
                    mView.setVisibility(View.VISIBLE);
                    mWeatherHighTempTextView.setText(highTemp);
                    mWeatherLowTempTextView.setText(lowTemp);
                    mDateTextView.setText(date);
                }
            }
        }
    }

    private class SetBitmapAsync extends AsyncTask<Asset, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Asset... params) {
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, params[0]).await().getInputStream();
            return BitmapFactory.decodeStream(assetInputStream);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mWeatherIconImageView.setImageBitmap(bitmap);
        }
    }
}

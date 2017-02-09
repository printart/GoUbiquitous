package com.example.android.sunshine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

/**
 * Created by printArt
 * ref->https://developer.android.com/training/building-wearables.html
 */
public class WearDataPush implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    //private int mCount;

    public WearDataPush(Context context) {
        mContext = context.getApplicationContext();
        setGoogleApiClient();
    }

    private void setGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void setWatchData(String date, String description, String high, String low, int iconId) {
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/watchWeatherData");
        DataMap dataMap = dataMapRequest.getDataMap();
        dataMap.putString("date", date);
        dataMap.putString("description", description);
        dataMap.putString("high", high);
        dataMap.putString("low", low);
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), iconId);
        Asset asset = createAsset(bitmap);
        dataMap.putAsset("weather_icon", asset);

        /*//testing
        /*//******************************
        mCount = 0;
        mCount++;
        dataMap.putInt("count", mCount);
        /*//*******************************/
        Wearable.DataApi.putDataItem(mGoogleApiClient, dataMapRequest.asPutDataRequest());
    }

    private Asset createAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    public void googleApiClientDisconnect() {
        mGoogleApiClient.disconnect();
    }
}

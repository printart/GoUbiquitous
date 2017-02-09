package com.example.android.sunshine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.TextUtils;
import android.view.SurfaceHolder;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by printArt
 */

public class BasicWatchFace extends CanvasWatchFaceService {

    @Override
    public CanvasWatchFaceService.Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {
        private GoogleApiClient mGoogleApiClient;
        private String mHighTemp;
        private String mLowTemp;
        private String mWeatherDate;
        private Bitmap mBitmap;
        private SimpleDateFormat mSimpleDateFormat;
        private Date mDate;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mGoogleApiClient = new GoogleApiClient.Builder(BasicWatchFace.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
            mDate = new Date();
            mSimpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            canvas.drawColor(ContextCompat.getColor(BasicWatchFace.this, R.color.colorWBackground));//background
            int width = bounds.width();
            int height = bounds.height();
            int centerX = width / 2;
            int centerY = height / 2;
            setTime(canvas, centerX, centerY);

            if (TextUtils.isEmpty(mHighTemp)) {
                noDataMessage(canvas, centerX, centerY);
            } else {
                setWeatherDate(canvas, centerX, centerY);
                drawLine(canvas, centerX, centerY);
                setWeatherDataHighTemperature(canvas, centerX, centerY);
                setWeatherDataLowTemperature(canvas, centerX, centerY);
                if (mBitmap != null) {
                    setWeatherIcon(canvas, centerX, centerY);
                }
            }
        }

        private void setTime(Canvas canvas, int... xy) {
            int centerX = xy[0];
            int centerY = xy[1];
            String time = mSimpleDateFormat.format(mDate);
            Paint minPaint = new Paint();
            minPaint.setColor(ContextCompat.getColor(BasicWatchFace.this, R.color.colorTime));
            minPaint.setAntiAlias(true);
            minPaint.setTextSize(45);
            minPaint.setFakeBoldText(true);
            canvas.drawText(time, centerX - 50, centerY - 50, minPaint);
        }

        private void drawLine(Canvas canvas, int... xy) {
            int centerX = xy[0];
            int centerY = xy[1];
            Paint line = new Paint();
            line.setColor(ContextCompat.getColor(BasicWatchFace.this, R.color.colorLine));
            line.setStrokeWidth(1.0f);
            line.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(centerX - 10, centerY + 10, centerX + 20, centerY + 10, line);
        }

        private void noDataMessage(Canvas canvas, int... xy) {
            int centerX = xy[0];
            int centerY = xy[1];
            Paint noDataMessage = new Paint();
            noDataMessage.setAntiAlias(true);
            noDataMessage.setColor(ContextCompat.getColor(BasicWatchFace.this, R.color.colorDate));
            noDataMessage.setTextSize(23);
            canvas.drawText("Waiting for data", centerX - 80, centerY, noDataMessage);
        }

        private void setWeatherDate(Canvas canvas, int... xy) {
            int centerX = xy[0];
            int centerY = xy[1];
            Paint weatherPaint = new Paint();
            weatherPaint.setTextSize(25);
            weatherPaint.setColor(ContextCompat.getColor(BasicWatchFace.this, R.color.colorDate));
            weatherPaint.setAntiAlias(true);
            canvas.drawText(mWeatherDate, centerX - 80, centerY, weatherPaint);
        }

        private void setWeatherDataHighTemperature(Canvas canvas, int... xy) {
            int centerX = xy[0];
            int centerY = xy[1];
            Paint weatherPaint = new Paint();
            weatherPaint.setTextSize(30);
            weatherPaint.setColor(ContextCompat.getColor(BasicWatchFace.this, R.color.colorHigh));
            weatherPaint.setAntiAlias(true);
            canvas.drawText(mHighTemp, centerX - 30, centerY + 60, weatherPaint);
        }

        private void setWeatherDataLowTemperature(Canvas canvas, int... xy) {
            int centerX = xy[0];
            int centerY = xy[1];
            Paint weatherPaint = new Paint();
            weatherPaint.setTextSize(30);
            weatherPaint.setColor(ContextCompat.getColor(BasicWatchFace.this, R.color.colorLow));
            weatherPaint.setAntiAlias(true);
            canvas.drawText(mLowTemp, centerX + 30, centerY + 60, weatherPaint);
        }

        private void setWeatherIcon(Canvas canvas, int... xy) {
            int centerX = xy[0];
            int centerY = xy[1];
            canvas.drawBitmap(mBitmap, centerX - 85, centerY + 25, null);

        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            for (DataEvent event : dataEventBuffer) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    String path = event.getDataItem().getUri().getPath();
                    if (path.equals("/watchWeatherData")) {
                        invalidate();
                        mHighTemp = dataMap.getString("high");
                        mLowTemp = dataMap.getString("low");
                        mWeatherDate = dataMap.getString("date");
                        Asset asset = dataMap.getAsset("weather_icon");
                        new SetBitmapAsync().execute(asset);
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
                if (bitmap != null) {
                    Matrix matrix = new Matrix();
                    matrix.postScale(0.6f, 0.6f);
                    mBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                    invalidate();
                }
            }
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
    }
}


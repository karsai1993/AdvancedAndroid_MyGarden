package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

/**
 * Created by Laci on 05/04/2018.
 */

public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor mCursor;

    public GridRemoteViewsFactory(Context context) {
        mContext = context;
    }


    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        Uri PLANT_URI = PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();
        if (mCursor != null) mCursor.close();
        mCursor = mContext.getContentResolver().query(
                PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_CREATION_TIME
        );
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (mCursor == null || mCursor.getCount() == 0) return null;
        mCursor.moveToPosition(position);
        long plantId = mCursor.getLong(
                mCursor.getColumnIndex(PlantContract.PlantEntry._ID)
        );
        int plantType = mCursor.getInt(
                mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE)
        );
        long createdAt = mCursor.getLong(
                mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME)
        );
        long wateredAt = mCursor.getLong(
                mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME)
        );
        long timeNow = System.currentTimeMillis();

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

        int imgRes = PlantUtils.getPlantImageRes(
                mContext,
                timeNow - createdAt,
                timeNow - wateredAt,
                plantType
        );
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));
        views.setViewVisibility(R.id.widget_water_button, View.GONE);

        Bundle bundle = new Bundle();
        bundle.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(bundle);
        views.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

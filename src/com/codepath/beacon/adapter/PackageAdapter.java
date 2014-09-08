package com.codepath.beacon.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;

public class PackageAdapter extends BaseAdapter {

    private List<PackageItem> data;
    private Context context;

    public PackageAdapter(Context context, List<PackageItem> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public PackageItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PackageItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.app_item, parent, false);
            holder = new ViewHolder();
            holder.ivImage = (ImageView) convertView.findViewById(R.id.example_row_iv_image);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.example_row_tv_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (getItem(position).getIsSelected()) {
        	convertView.setBackgroundColor(Color.YELLOW);
        } else {
        	convertView.setBackgroundColor(convertView.getResources().getColor(R.color.natural));
        }
        ApplicationInfo appInfo = item.getAppInfo();
        Drawable appIcon = appInfo.loadIcon(context.getPackageManager());
        Bitmap bitmap = drawableToBitmap(appIcon, 96, 96);
        holder.ivImage.setImageBitmap(bitmap);
        holder.tvTitle.setText(item.getName());
        return convertView;
    }

    static class ViewHolder {
        ImageView ivImage;
        TextView tvTitle;
    }

    private boolean isPlayStoreInstalled() {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(market, 0);

        return list.size() > 0;
    }
    
    public static Bitmap drawableToBitmap (Drawable drawable, int reqWidth, int reqHeight) {
    	if (drawable instanceof BitmapDrawable) {
    		Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
    		if (drawable.getIntrinsicHeight() > 96) {
    			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
    			bitmap = scaledBitmap;
    		}
    		return bitmap;
    	}

        Bitmap bitmap = Bitmap.createBitmap(reqWidth, reqHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); 
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
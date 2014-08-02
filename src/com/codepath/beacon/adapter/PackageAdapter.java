package com.codepath.beacon.adapter;

import java.util.List;

import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
//            holder.tvDescription = (TextView) convertView.findViewById(R.id.example_row_tv_description);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (getItem(position).getIsSelected()) {
        	convertView.setBackgroundColor(Color.YELLOW);
        } else {
        	convertView.setBackgroundColor(convertView.getResources().getColor(R.color.natural));
        }

        holder.ivImage.setImageDrawable(item.getIcon());
        holder.tvTitle.setText(item.getName());
//        holder.tvDescription.setText(item.getPackageName());
        return convertView;
    }

    static class ViewHolder {
        ImageView ivImage;
        TextView tvTitle;
//        TextView tvDescription;
    }

    private boolean isPlayStoreInstalled() {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(market, 0);

        return list.size() > 0;
    }

}
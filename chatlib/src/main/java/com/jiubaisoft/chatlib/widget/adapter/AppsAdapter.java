package com.jiubaisoft.chatlib.widget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubaisoft.chatlib.R;
import com.jiubaisoft.chatlib.entity.AppEntity;

import java.util.ArrayList;

public class AppsAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context mContext;
    private ArrayList<AppEntity> mDdata = new ArrayList<AppEntity>();
    private Callback callback;

    public AppsAdapter(Context context, ArrayList<AppEntity> data) {
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        if (data != null) {
            this.mDdata = data;
        }
    }

    @Override
    public int getCount() {
        return mDdata.size();
    }

    @Override
    public Object getItem(int position) {
        return mDdata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_app, null);
            viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final AppEntity appBean = mDdata.get(position);
        if (appBean != null) {
            viewHolder.iv_icon.setBackgroundResource(appBean.getIcon());
            viewHolder.tv_name.setText(appBean.getFuncName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(callback == null){
                        return;
                    }

                    callback.onAppClickItem(appBean);

                }
            });
        }
        return convertView;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    class ViewHolder {
        public ImageView iv_icon;
        public TextView tv_name;
    }

    public interface Callback{
        void onAppClickItem(AppEntity appEntity);
    }
}
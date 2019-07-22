package com.jiubaisoft.chatlib.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;


import com.jiubaisoft.chatlib.R;
import com.jiubaisoft.chatlib.entity.AppEntity;
import com.jiubaisoft.chatlib.widget.adapter.AppsAdapter;

import java.util.ArrayList;

public class SimpleAppsGridView extends RelativeLayout {

    protected View view;

    private AppsAdapter.Callback callback;

    private ArrayList<AppEntity> mAppBeanList;

    private AppsAdapter adapter;

    public SimpleAppsGridView(Context context) {
        super(context);
    }

    public SimpleAppsGridView(Context context, AppsAdapter.Callback callback){
        this(context);
        this.callback = callback;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_apps, this);
        init();
    }

    protected void init(){
        GridView gv_apps = view.findViewById(R.id.gv_apps);
        mAppBeanList = new ArrayList<>();
        adapter = new AppsAdapter(getContext(), mAppBeanList);
        adapter.setCallback(callback);
        gv_apps.setAdapter(adapter);
    }

    /**
     * 添加item内容
     * @param 
     * @return 
     * @throws 
     * @date  
     */
    public SimpleAppsGridView addItem(AppEntity appEntity){
        mAppBeanList.add(appEntity);
        adapter.notifyDataSetChanged();
        return this;
    }
}

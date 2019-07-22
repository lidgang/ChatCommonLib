package com.jiubaisoft.chatlibrary;

import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jiubaisoft.chatlib.callback.RecordCallback;
import com.jiubaisoft.chatlib.constants.Constants;
import com.jiubaisoft.chatlib.entity.AppEntity;
import com.jiubaisoft.chatlib.utils.PermissionUtil;
import com.jiubaisoft.chatlib.widget.SimpleUserdefEmoticonsKeyBoard;
import com.jiubaisoft.chatlib.widget.adapter.AppsAdapter;

/**
 * 描述：聊天测试
 *
 * @author lidagang
 *
 * @ClassName:  com.jiubaisoft.chatlibrary.MainActivity
 *
 * @date 2019-07-18 10:09
 *
 */
public class MainActivity extends AppCompatActivity implements AppsAdapter.Callback {

    private SimpleUserdefEmoticonsKeyBoard keyBoard;

    private boolean isRecordInit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化信息
        initView();
    }

    /**
     * 初始化界面
     * @param 
     * @return 
     * @throws 
     * @date  
     */
    private void initView() {
        keyBoard = findViewById(R.id.emokeyboard);
        //初始化表情
        keyBoard.initDefaultEmoji();
        //初始化应用
        keyBoard.initApps(this)
                .addItem(new AppEntity(R.mipmap.icon_photo,"图片"))
                .addItem(new AppEntity(R.mipmap.icon_audio,"视频"));
        keyBoard.initRecord(this, new RecordCallback() {
            @Override
            public void recordResult(String path, long time) {
                Toast.makeText(MainActivity.this, path+",时长："+time, Toast.LENGTH_SHORT).show();
            }
        });

        //检查存储权限
        if (!PermissionUtil.hasSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            String[] pp = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(this, pp, Constants.PERMISSIONS_REQUEST_AUDIO);
        }
    }

    @Override
    public void onAppClickItem(AppEntity appEntity) {
        Toast.makeText(this, "选择了："+appEntity.getFuncName(), Toast.LENGTH_SHORT).show();
    }
}

# 用于实现聊天的工具包
[![](https://jitpack.io/v/lidgang/ChatCommonLib.svg)](https://jitpack.io/#lidgang/ChatCommonLib)

###### 聊天栏功能说明
主要实现聊天栏的相关效果和功能，可以直接添加配置后结合RecyclerView直接实现聊天对话框界面。
主要包含：
1. 聊天输入框的界面效果和相应交互功能
2. 语音消息的录制
3. 基本表情包
4. 自定义添加其他功能，发送图片、视频、位置等等
###### 具体效果如下图：
![944a44d7c7d49b0266a7008cd1c95044.gif](evernotecid://5EFB1936-4904-43EA-813E-27B5AC904CDD/appyinxiangcom/976820/ENResource/p892)
###### 引用库
Step 1.在Build中添加jitpack支持
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. 添加dependency
```
dependencies {
	        implementation 'com.github.lidgang:ChatCommonLib:1.0.6'
	}
```
###### 使用说明
实现聊天对话框时，调用自定义控件即可。
```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.jiubaisoft.chatlib.widget.SimpleUserdefEmoticonsKeyBoard
        android:id="@+id/emokeyboard"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ListView
            android:layout_width="match_parent"
            android:layout_height="match_parent"></ListView>

    </com.jiubaisoft.chatlib.widget.SimpleUserdefEmoticonsKeyBoard>

</RelativeLayout>
```

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

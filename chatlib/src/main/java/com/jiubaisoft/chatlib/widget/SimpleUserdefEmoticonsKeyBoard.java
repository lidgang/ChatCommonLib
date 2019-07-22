package com.jiubaisoft.chatlib.widget;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.czt.mp3recorder.Mp3Recorder;
import com.jiubaisoft.chatlib.R;
import com.jiubaisoft.chatlib.callback.RecordCallback;
import com.jiubaisoft.chatlib.constants.Constants;
import com.jiubaisoft.chatlib.emoji.EmojiBean;
import com.jiubaisoft.chatlib.utils.EmoCommonUtils;
import com.jiubaisoft.chatlib.utils.PermissionUtil;
import com.jiubaisoft.chatlib.widget.adapter.AppsAdapter;
import com.jiubaisoft.chatlib.widget.dialog.RecordDialogManager;

import sj.keyboard.XhsEmoticonsKeyBoard;
import sj.keyboard.data.EmoticonEntity;
import sj.keyboard.interfaces.EmoticonClickListener;
import sj.keyboard.utils.EmoticonsKeyboardUtils;

public class SimpleUserdefEmoticonsKeyBoard extends XhsEmoticonsKeyBoard {

    public final int APPS_HEIGHT = 120;

    // 当前状态，默认为正常
    private int mCurrentState = STATE_NORMAL;
    private static final int STATE_NORMAL = 100001;
    private static final int STATE_RECORDING = 100002;
    private static final int STATE_WANT_CANCEL = 100003;
    private static final int MSG_AUDIO_PREPARED = 100004;
    private static final int MSG_VOICE_CHANGE = 100005;
    private static final int MSG_DIALOG_DISMISS = 100006;

    private static final int CANCEL_HEIGHT = 50;

    private AudioManager mAudioManager;
    /**音频缓存地址*/
    private String mAudioSaveDir = "/mnt/sdcard/temp/icon_voice/";

    private boolean isReady = false;
    private boolean isRecording = false;
    private long mRecordTime;

    private RecordDialogManager mDialogManager;

    private Mp3Recorder mRecorderManager;

    private Activity mActivity;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    // 录音管理器 prepare 成功，开始录音并显示dialog
                    // 启动线程记录时间并获取音量变化
                    isRecording = true;
                    mRecorderManager.startRecording();
                    mDialogManager.showDialogRecord();
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_VOICE_CHANGE:
                    mDialogManager.updateVoiceLevel((Integer) msg.obj);
                    break;
                case MSG_DIALOG_DISMISS:
                    mDialogManager.dismissDialog();
                    break;
            }
        }
    };

    public SimpleUserdefEmoticonsKeyBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 初始化默认表情
     * @param 
     * @return 
     * @throws 
     * @date  
     */
    public void initDefaultEmoji() {
        EmoCommonUtils.initEmoticonsEditText(getEtChat());
        setAdapter(EmoCommonUtils.getCommonAdapter(getContext(),emoticonClickListener));
        //隐藏表情工具栏
        getEmoticonsToolBarView().setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) getEmoticonsToolBarView().getLayoutParams();
        linearParams.height = 0;
        getEmoticonsToolBarView().setLayoutParams(linearParams);
    }

    /**
     * 初始化其它发送类型
     * @param
     * @return
     * @throws
     * @date
     */
    public SimpleAppsGridView initApps(AppsAdapter.Callback callback){
        SimpleAppsGridView simpleAppsGridView = new SimpleAppsGridView(getContext(),callback);
        addFuncView(simpleAppsGridView);
        return simpleAppsGridView;
    }
    
    public void initRecord(Activity activity,final RecordCallback callback){
        this.mActivity = activity;
        // 初始化 dialog 管理器
        mDialogManager = new RecordDialogManager(getContext());
        // 获取音频管理，以申请音频焦点
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        // 初始化录音管理器
        mRecorderManager = new Mp3Recorder(mAudioSaveDir);
        mRecorderManager.setListener(recorderListener);

        getBtnVoice().setText(getResources().getString(R.string.press_record));
        getBtnVoice().setBackgroundResource(R.drawable.record_button_normal);
        // 设置按钮长按事件监听，只有触发长按才开始准备录音
        getBtnVoice().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 获取焦点
                int focus = mAudioManager.requestAudioFocus(null,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                if (focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    isReady = true;
                    mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
                }
                return false;
            }
        });

        getBtnVoice().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (!onRecordPrepare()) {
                    return true;
                }

                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        changeState(STATE_RECORDING);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isRecording) {
                            if (isWantToCancel(x, y)) {
                                changeState(STATE_WANT_CANCEL);
                            } else {
                                changeState(STATE_RECORDING);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 未触发 longClick,直接重置
                        if (!isReady) {
                            resetRecord();
                            return false;
                        }
                        // 触发了longClick，开始初始化录音，但是为初始化完成,或者录音时间太短
                        if (!isRecording || mRecordTime < 1) {
                            mDialogManager.showDialogToShort();
                            mRecorderManager.stopRecording();
                            mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1000);
                        } else if (mCurrentState == STATE_RECORDING) {
                            mDialogManager.dismissDialog();
                            mRecorderManager.stopRecording();
                            // 将录音文件路径和录音时长回调
                            callback.recordResult(mRecorderManager.getFilePath(), mRecordTime);
                        } else if (mCurrentState == STATE_WANT_CANCEL) {
                            mDialogManager.dismissDialog();
                            mRecorderManager.stopRecording();
                        }
                        resetRecord();
                        break;
                }

                return false;
            }
        });
    }


    @Override
    protected void inflateKeyboardBar(){
        mInflater.inflate(R.layout.view_keyboard_userdef, this);
    }

    @Override
    protected View inflateFunc(){
        return mInflater.inflate(R.layout.view_func_emoticon_userdef, null);
    }

    @Override
    public void reset() {
        EmoticonsKeyboardUtils.closeSoftKeyboard(getContext());
        mLyKvml.hideAllFuncView();
        mBtnFace.setImageResource(R.drawable.chatting_emoticons);
    }

    @Override
    public void onFuncChange(int key) {
        if (FUNC_TYPE_EMOTION == key) {
            mBtnFace.setImageResource(R.drawable.chatting_softkeyboard);
        } else {
            mBtnFace.setImageResource(R.drawable.chatting_emoticons);
        }
        checkVoice();
    }

    @Override
    public void OnSoftClose() {
        super.OnSoftClose();
        if (mLyKvml.getCurrentFuncKey() == FUNC_TYPE_APPPS) {
            setFuncViewHeight(EmoticonsKeyboardUtils.dip2px(getContext(), APPS_HEIGHT));
        }
    }

    @Override
    protected void showText() {
        mEtChat.setVisibility(VISIBLE);
        mBtnFace.setVisibility(VISIBLE);
        mBtnVoice.setVisibility(GONE);
    }

    @Override
    protected void showVoice() {
        mEtChat.setVisibility(GONE);
        mBtnFace.setVisibility(GONE);
        mBtnVoice.setVisibility(VISIBLE);
        reset();
    }

    @Override
    protected void checkVoice() {
        if (mBtnVoice.isShown()) {
            mBtnVoiceOrText.setImageResource(R.drawable.chatting_softkeyboard);
        } else {
            mBtnVoiceOrText.setImageResource(R.drawable.chatting_vodie);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == com.keyboard.view.R.id.btn_voice_or_text) {
            if (mEtChat.isShown()) {
                mBtnVoiceOrText.setImageResource(R.drawable.chatting_softkeyboard);
                showVoice();
            } else {
                showText();
                mBtnVoiceOrText.setImageResource(R.drawable.chatting_vodie);
                EmoticonsKeyboardUtils.openSoftKeyboard(mEtChat);
            }
        } else if (i == com.keyboard.view.R.id.btn_face) {
            toggleFuncView(FUNC_TYPE_EMOTION);
        } else if (i == com.keyboard.view.R.id.btn_multimedia) {//表情修改
            toggleFuncView(FUNC_TYPE_APPPS);
            setFuncViewHeight(EmoticonsKeyboardUtils.dip2px(getContext(), APPS_HEIGHT));
        }
    }

    /**选中的表情处理*/
    EmoticonClickListener emoticonClickListener = new EmoticonClickListener() {
        @Override
        public void onEmoticonClick(Object o, int actionType, boolean isDelBtn) {

            if (isDelBtn) {
                EmoCommonUtils.delClick(getEtChat());
            } else {
                if (o == null) {
                    return;
                }
                if (actionType != Constants.EMOTICON_CLICK_BIGIMAGE) {
                    String content = null;
                    if (o instanceof EmojiBean) {
                        content = ((EmojiBean) o).emoji;
                    } else if (o instanceof EmoticonEntity) {
                        content = ((EmoticonEntity) o).getContent();
                    }

                    if (TextUtils.isEmpty(content)) {
                        return;
                    }
                    int index = getEtChat().getSelectionStart();
                    Editable editable = getEtChat().getText();
                    editable.insert(index, content);
                }
            }
        }
    };

    /**
     * 判断是否是要取消
     *
     * @param x 手指当前位置 x 坐标
     * @param y 手指当前位置 y 坐标
     */
    private boolean isWantToCancel(int x, int y) {
        return x < 0 || x > getBtnVoice().getWidth()
                || y < -CANCEL_HEIGHT || y > getBtnVoice().getHeight() + CANCEL_HEIGHT;
    }

    /**
     * 改变语音状态
     * @param
     * @return
     * @throws
     * @date
     */
    private void changeState(int state) {
        if (mCurrentState != state) {
            mCurrentState = state;
            if (state == STATE_NORMAL) {
                getBtnVoice().setText(getResources().getString(R.string.press_record));
                getBtnVoice().setBackgroundResource(R.drawable.record_button_normal);
            } else if (state == STATE_RECORDING) {
                getBtnVoice().setText(getResources().getString(R.string.release_end));
                getBtnVoice().setBackgroundResource(R.drawable.record_button_recoding);
                if (isRecording) {
                    mDialogManager.showRecording();
                }
            } else if (state == STATE_WANT_CANCEL) {
                getBtnVoice().setText(getResources().getString(R.string.release_cancel));
                getBtnVoice().setBackgroundResource(R.drawable.record_button_recoding);
                if (isRecording) {
                    mDialogManager.showDialogWantCancel();
                }
            }
        }
    }

    /**
     * 释放资源，释放音频焦点
     */
    private void resetRecord() {
        isReady = false;
        isRecording = false;
        mRecordTime = 0;
        changeState(STATE_NORMAL);

        // 释放焦点
        if (mAudioManager != null){
            mAudioManager.abandonAudioFocus(null);
        }
    }

    // 子线程 runnable，每隔0.1秒获取音量大小，并记录录音时间
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(1000);
                    mRecordTime += 1000;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Mp3Recorder.RecorderListener recorderListener = new Mp3Recorder.RecorderListener() {
        @Override
        public void stop() {
            //取消录制
        }

        @Override
        public void Volume(int volume) {
            Message msg = new Message();
            msg.obj = volume;
            msg.what = MSG_VOICE_CHANGE;
            mHandler.sendMessage(msg);
        }
    };

    /**
     * 控制权限
     * @param
     * @return
     * @throws
     * @date
     */
    public boolean onRecordPrepare() {
        //检查录音权限
        if (!PermissionUtil.hasSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)) {
            String[] pp = new String[]{
                    Manifest.permission.RECORD_AUDIO
            };
            ActivityCompat.requestPermissions(mActivity, pp, Constants.PERMISSIONS_REQUEST_AUDIO);
            return false;
        }
        return true;
    }
}

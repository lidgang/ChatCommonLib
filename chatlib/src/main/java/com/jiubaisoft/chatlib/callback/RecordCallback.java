package com.jiubaisoft.chatlib.callback;
/**
 * 描述：语音回调
 *
 * @author lidagang
 *
 * @ClassName:  com.jiubaisoft.chatlib.callback.RecordCallback
 *
 * @date 2019-07-22 13:01
 *
 */
public interface RecordCallback {

    /**
     * 语音录制回掉
     * @param path 语音地址
     * @param time 录制时长
     * @return 
     * @throws 
     * @date  
     */
    void recordResult(String path,long time);

}

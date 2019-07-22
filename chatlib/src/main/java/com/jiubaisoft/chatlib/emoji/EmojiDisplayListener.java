package com.jiubaisoft.chatlib.emoji;

import android.content.Context;
import android.text.Spannable;

public interface EmojiDisplayListener {

	void onEmojiDisplay(Context context, Spannable spannable, String emojiHex, int fontSize, int start, int end);
}

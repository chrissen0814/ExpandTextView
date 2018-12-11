package com.chrissen.expandtextview.expandtextview;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.chrissen.expandtextview.App;
import com.chrissen.expandtextview.R;
import com.chrissen.expandtextview.util.MoonUtil;
import com.chrissen.expandtextview.util.UIUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Function:
 * <br/>
 * Describe:
 * <br/>
 * Author: John on 16/4/18.
 * <br/>
 * Email: john@jiuhuar.com
 */
public class ExpandTextView extends android.support.v7.widget.AppCompatTextView {

    private static final int CORE_POOL_SIZE = 5;
    /**
     * 默认执行最大线程是128个
     */
    private static final int MAXIMUM_POOL_SIZE = 128;

    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            int count = mCount.getAndIncrement();
            Log.v(getClass().getSimpleName(),"new Thread " + "ExpandTextView #" + count);
            return new Thread(r, "ExpandTextView #" + count);
        }
    };

    /**
     * 执行队列，默认是10个，超过10个后会开启新的线程，如果已运行线程大于 {@link #MAXIMUM_POOL_SIZE}，执行异常策略
     */
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);

    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            sPoolWorkQueue, sThreadFactory);

    public LruMemoryCache<String, SpannableStringBuilder> stringMemoryCache;

    private EmotionTask emotionTask;

    private String content;

    public ExpandTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExpandTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandTextView(Context context) {
        super(context);
    }

    public void setContent(String text) {
        if (stringMemoryCache == null) {
            stringMemoryCache = new LruMemoryCache<String, SpannableStringBuilder>(200) {
            };
        }

        content = text;

        if (TextUtils.isEmpty(content))
            return;

        //同一个AsyncTask只能执行一次
        if (emotionTask != null)
            emotionTask.cancel(true);

        String key = KeyGenerator.generateMD5(text);
        SpannableStringBuilder spannableString = stringMemoryCache.get(key);
        if (spannableString != null) {
            Log.v(getClass().getSimpleName(),"从内存中加载spannable数据");
            super.setText(spannableString);
        } else {
            Log.v(getClass().getSimpleName(),"开启线程，开始加载spannable数据");
            super.setText(text);
            emotionTask = new EmotionTask(this);
            emotionTask.executeOnExecutor(THREAD_POOL_EXECUTOR);
        }

        setClickable(false);
    }


    class EmotionTask extends WorkTask<Void, SpannableStringBuilder, Boolean> {

        WeakReference<TextView> textViewRef;

        EmotionTask(TextView textView) {
            textViewRef = new WeakReference<>(textView);
        }

        @Override
        public Boolean workInBackground(Void... params) throws TaskException {
            TextView textView = textViewRef.get();
            if (textView == null)
                return false;

            if (TextUtils.isEmpty(textView.getText()))
                return false;

            // android.view.ViewRootImpl$CalledFromWrongThreadException Only the original thread that created a view hierarchy can touch its views.
            // 把getText + 一个空字符试试，可能是直接取值会刷UI
            //在之前已经调用super.setText(text)设置content
            String text = textView.getText().toString();
            int emojiSize = UIUtils.getFontHeight(textView);
            //替换表情
            SpannableString spannableString = MoonUtil.replaceEmoticonsBySize(textView.getContext(), text, emojiSize, ImageSpan.ALIGN_BOTTOM);

            int accentColor = App.getContext().getResources().getColor(R.color.green);

            // 用户@
            Pattern atPattern = Pattern.compile("@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}");
            Matcher atMatcher = atPattern.matcher(spannableString);
            while (atMatcher.find()){
                int start = atMatcher.start();
                int end = atMatcher.end();
                spannableString.setSpan(new ForegroundColorSpan(accentColor),start,end,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            //话题替换
            Pattern topicPattern = Pattern.compile("#[a-zA-Z_\\u4e00-\\u9fa5]{3,30}# ");
            Matcher topicMatcher = topicPattern.matcher(spannableString);
            while (topicMatcher.find()){
                int start = topicMatcher.start();
                int end = topicMatcher.end();
                spannableString.setSpan(new ForegroundColorSpan(accentColor),start,end,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(spannableString);
            String key = KeyGenerator.generateMD5(spannableStringBuilder.toString());
            stringMemoryCache.put(key, spannableStringBuilder);

            publishProgress(spannableStringBuilder);
            return null;
        }

        @Override
        protected void onProgressUpdate(SpannableStringBuilder... values) {
            super.onProgressUpdate(values);

            TextView textView = textViewRef.get();
            if (textView == null) {
                return;
            }

            try {
                if (values != null && values.length > 0) {
                    textView.setText(values[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}

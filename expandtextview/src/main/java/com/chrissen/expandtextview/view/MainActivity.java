package com.chrissen.expandtextview.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chrissen.expandtextview.R;
import com.chrissen.expandtextview.expandtextview.ExpandTextView;


public class MainActivity extends AppCompatActivity {

    private ExpandTextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text_view);
        String content = "@CP3 " + "Hello World!" + "[流汗][脸红][开心][便便][大笑][18禁][开心][吐舌]";
        mTextView.setContent(content);
    }
}

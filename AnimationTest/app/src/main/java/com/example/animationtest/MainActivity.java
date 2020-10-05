package com.example.animationtest;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Animation animation;
    private TranslateAnimation translateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        animation = AnimationUtils.loadAnimation(this, R.anim.translate_animation);

        Button buttonFadeIn = findViewById(R.id.button);
        buttonFadeIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                imageView.startAnimation(animation);
            }
        });

        Button buttonFadeIn2 = findViewById(R.id.button2);
        buttonFadeIn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startTranslate();
            }
        });
    }
    private void startTranslate(){
        // 設定を切り替え可能
        int type = 0;
        if(type == 0){
            // TranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue, int fromYType, float fromYValue, int toYType, float toYValue)
            translateAnimation = new TranslateAnimation(
                    Animation.ABSOLUTE, 0.0f,
                    Animation.ABSOLUTE, 500.0f,
                    Animation.ABSOLUTE, 0.0f,
                    Animation.ABSOLUTE, 1200.0f);
        }
        else if(type == 1){
            translateAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.9f,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.8f);
        }
        else if(type ==2){
            translateAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.4f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.6f);
        }

        // animation時間 msec
        translateAnimation.setDuration(2000);
        // 繰り返し回数
        translateAnimation.setRepeatCount(10);
        // animationが終わったそのまま表示にする
        translateAnimation.setFillAfter(false);
        //アニメーションの開始
        imageView.startAnimation(translateAnimation);
    }
}
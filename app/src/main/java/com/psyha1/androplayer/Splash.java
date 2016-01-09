package com.psyha1.androplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * Created by HaroldHibari on 25/11/2015.
 * In this file I create and animate a splash (loading) screen for the app.
 * I make use of the animation library to animate images I place on the splash screen
 */
public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        //load images
        final ImageView logo = (ImageView) findViewById(R.id.imageView1);
        final ImageView img = (ImageView) findViewById(R.id.imageView2);

        //load the animation I created: "rotate" and fade out effect
        final Animation anim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);
        final Animation anim1 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.abc_fade_out);

        //set animations to each specific image
        img.startAnimation(anim);

        //create a listener to switch to the main activity from the splash screen
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                logo.startAnimation(anim1);
                img.startAnimation(anim1);
                Intent i = new Intent(getBaseContext(), MainActivity.class); //start the main app
                startActivity(i);
                Splash.this.finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }
}

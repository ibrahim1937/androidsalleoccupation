package ma.ibrahimchahboune.salleoccupationqr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import ma.ibrahimchahboune.salleoccupationqr.beans.User;
import ma.ibrahimchahboune.salleoccupationqr.utils.DataHolder;

public class SplashScreen extends AppCompatActivity {



    TextView title;
    LottieAnimationView image;
    ImageView bgImg;
    private DataHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        title = findViewById(R.id.splash_title);
        image = findViewById(R.id.lottieAnimationView);
        bgImg = findViewById(R.id.bg_img);




        title.animate().translationY(1400).setDuration(1000).setStartDelay(4000);
        image.animate().translationY(1400).setDuration(1000).setStartDelay(4000);
        bgImg.animate().translationY(-2000).setDuration(1000).setStartDelay(4000);



        new Thread(){
            @Override
            public void run() {
                try {
                    sleep(5000);
                    Intent intent = new Intent(SplashScreen.this, SignInActivity.class);
                    startActivity(intent);
                    SplashScreen.this.finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
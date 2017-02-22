package com.linsaya.view_guaguaka;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.linsaya.view_guaguaka.view.Guaguaka;

public class MainActivity extends AppCompatActivity {
    private Guaguaka guaguaka;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Guaguaka guaguaka = (Guaguaka) findViewById(R.id.guaguaka);
        guaguaka.setOnCompleteListener(new Guaguaka.OnCompleteListener() {
            @Override
            public void onComplete() {
                Toast.makeText(getApplicationContext(),"恭喜你中奖啦！",Toast.LENGTH_SHORT).show();
            }
        });
        guaguaka.setText("恭喜你获得一台华为Mate9！");
    }
}

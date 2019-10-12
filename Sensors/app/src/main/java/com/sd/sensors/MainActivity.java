package com.sd.sensors;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.GridLayout;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    GridLayout mainGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainGrid = (GridLayout) findViewById(R.id.mainGrid);

        //Set Event
        setSingleEvent(mainGrid);
    }

    private void setSingleEvent(GridLayout mainGrid) {
        //Loop all child item of Main Grid
        for (int i = 0; i < mainGrid.getChildCount(); i++) {
            //You can see , all child item is CardView , so we just cast object to CardView
            CardView cardView = (CardView) mainGrid.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (finalI == 0) {
                        Intent intent = new Intent(MainActivity.this, SoundActivity.class);
                        startActivity(intent);
                    }
                    else if (finalI == 1) {
                        Intent intent = new Intent(MainActivity.this, LedActivity.class);
                        startActivity(intent);
                    }
                    else if (finalI == 2) {
                        Intent intent = new Intent(MainActivity.this, MagneticActivity.class);
                        startActivity(intent);
                    }
                    else if (finalI == 3) {
                        Intent intent = new Intent(MainActivity.this, LightActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }
}

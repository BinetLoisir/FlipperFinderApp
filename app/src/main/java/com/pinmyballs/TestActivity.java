package com.pinmyballs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.pinmyballs.database.FlipperDatabaseHandler;


public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";

    EditText textInput;
    String modelIDString;
    Button searchButton;
    TextView textView;
    ImageView imageView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        textInput = findViewById(R.id.enterNumber);
        searchButton = findViewById(R.id.searchbutton);
        searchButton.setOnClickListener(view -> pressButton());
        textView = findViewById(R.id.pinNumber);
        imageView = findViewById(R.id.pinImage);


    }

    private void pressButton(){

       imageView.setImageResource(getResources().getIdentifier("@drawable/header_icon", null,this.getPackageName()));
       textView.setText("");

        modelIDString = textInput.getText().toString();

        int modelID = Integer.parseInt(modelIDString);

        loadImage(modelID);

    }

    private void loadImage(int modelID) {
        //Set image from Parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.MODELE_FLIPPER_ID,modelID);
        query.setLimit(1);
        query.findInBackground((modelList, e) -> {
            if (e == null) {
                Log.d("model", "Retrieved " + modelList.size() + " modeles");

                if (modelList.size()>0) {
                    ParseObject object = modelList.get(0);
                    String opbd = object.getString("MOFL_OPBDID");
                    String name = object.getString("MOFL_NOM");
                    textView.setText(name);
                    Log.d(TAG, "onCreate: "+ name+" " + opbd);
                    ParseFile fileObject = (ParseFile) object.get("MOFL_IMAGE");

                    if (fileObject != null) {
                        fileObject.getDataInBackground(new GetDataCallback() {

                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    // Decode the Byte[] into Bitmap
                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    // Set the Bitmap into the ImageView
                                    imageView.setImageBitmap(bmp);

                                } else { Log.d("test", "Problem load image the data.");
                                    textView.setText("Problem load image the data.");

                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "loadImage: no File ");
                        textView.setText("No file yet");

                    }
                } else {
                    Log.d(TAG, "test: Empty Query.");
                    textView.setText("No model with the this ID number");

                }

            } else {
                Log.d("model", "Error: " + e.getMessage());
            }
        });
    }
}



//TEST ACTIVITY FOR FRAGMENT TRANSACTION AND INTERFACES
/*public class TestActivity extends AppCompatActivity implements Fragment1.Fragment1Listener, Fragment2.Fragment2Listener {

    private Fragment1 fragment1;
    private Fragment2 fragment2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame1, fragment1)
                .replace(R.id.frame2, fragment2)
        .commit();
    }

    @Override
    public void sendTextToFragment2(CharSequence charSequence) {
        fragment2.UpdateEditText(charSequence);
    }

    @Override
    public void sendTextToFragment1(CharSequence charSequence) {
        fragment1.UpdateEditText(charSequence);
    }
}*/

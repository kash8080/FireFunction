package com.kashyapmedia.firebaselib;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.kashyapmedia.firebaselib.Models.ProfileResponseBody;
import com.kashyapmedia.firebaselib.Models.SampleRequestBody;
import com.kashyapmedia.firefunction.FireFunction;
import com.kashyapmedia.firefunction.Models.Call;
import com.kashyapmedia.firefunction.Models.Response;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SampleRequestBody requestBody=new SampleRequestBody("testid","testname");
        Call<ProfileResponseBody> data = FireFunction.getInstance().create(FirebaseInterface.class).getProfile(requestBody);
        data.execute(new Response<ProfileResponseBody>() {
            @Override
            public void onSuccess(ProfileResponseBody data) {
                Log.d(TAG, "onSuccess: name="+data.getName()+",age="+data.getAge());
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

    }

}



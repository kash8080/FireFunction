package com.kashyapmedia.firebaselib;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.kashyapmedia.firebaselib.Models.ProfileResponseBody;
import com.kashyapmedia.firebaselib.Models.SampleRequestBody;
import com.kashyapmedia.firefunction.FireFunction;
import com.kashyapmedia.firefunction.Models.Call;
import com.kashyapmedia.firefunction.Models.Response;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private CompositeDisposable disposables;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        disposables =new CompositeDisposable();

        FireFunction.debug=true;

        SampleRequestBody requestBody=new SampleRequestBody("testid","testname");
        FirebaseInterface myInterface= FireFunction.getInstance().create(FirebaseInterface.class);

        Call<ProfileResponseBody> data = myInterface.getProfile(requestBody);
        data.execute(new Response<ProfileResponseBody>() {
            @Override
            public void onSuccess(ProfileResponseBody data) {
                Log.d(TAG, "onSuccess: name="+data.getName()+",age="+data.getAge());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: ",e );
            }
        });

        myInterface.getProfileRX(requestBody)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ProfileResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: ");
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(ProfileResponseBody profileResponseBody) {
                        Log.d(TAG, "onNext: name="+profileResponseBody.getName()+",age="+profileResponseBody.getAge());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ",e );
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });

    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }
}



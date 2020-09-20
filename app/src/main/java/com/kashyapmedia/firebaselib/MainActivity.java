package com.kashyapmedia.firebaselib;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.kashyapmedia.firebaselib.Models.ProfileResponseBody;
import com.kashyapmedia.firebaselib.Models.SampleRequestBody;
import com.kashyapmedia.firefunction.FireFunction;
import com.kashyapmedia.firefunction.Models.Call;
import com.kashyapmedia.firefunction.Models.Response;
import com.kashyapmedia.firefunction.responses.ApiErrorResponse;
import com.kashyapmedia.firefunction.responses.ApiResponse;
import com.kashyapmedia.firefunction.responses.ApiSuccessResponse;

import java.util.HashMap;

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
        FirebaseInterface myInterface= FireFunction.getInstance()
                .setGlobalRequestListener(new FireFunction.GlobalRequestListener() {
                    @Override
                    public void onSuccess(String functionName, HashMap<String, Object> resultData) {
                        Log.d(TAG, "GlobalRequestListener onSuccess: functionName:"+functionName);
                    }

                    @Override
                    public void onError(String functionName, HashMap<String, Object> requestData, Exception e) {
                        Log.d(TAG, "GlobalRequestListener onError: functionName:"+functionName+" error:"+e.toString());

                    }
                })
                .create(FirebaseInterface.class);

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
/*
        data = myInterface.nonexistentfunction(requestBody);
        data.execute(new Response<ProfileResponseBody>() {
            @Override
            public void onSuccess(ProfileResponseBody data) {
                Log.d(TAG, "nonexistentfunction onSuccess: name=");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "nonexistentfunction onError: ",e );
            }
        });
*/
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


        myInterface.getProfileLiveData(requestBody)
                .observe(this, new androidx.lifecycle.Observer<ApiResponse<ProfileResponseBody>>() {
                    @Override
                    public void onChanged(ApiResponse<ProfileResponseBody> profileResponseBodyApiResponse) {
                        if(profileResponseBodyApiResponse instanceof ApiSuccessResponse){
                            Log.d(TAG, "getProfileLiveData success: "+((ApiSuccessResponse<ProfileResponseBody>) profileResponseBodyApiResponse).getBody().getName());

                        }else if(profileResponseBodyApiResponse instanceof ApiErrorResponse){
                            Log.e(TAG, "getProfileLiveData onError: "+((ApiErrorResponse<ProfileResponseBody>) profileResponseBodyApiResponse).getErrorMessage());
                        }
                    }
                });

    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }
}



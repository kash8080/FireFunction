package com.kashyapmedia.firebaselib;

import com.kashyapmedia.firebaselib.Models.ProfileResponseBody;
import com.kashyapmedia.firebaselib.Models.SampleRequestBody;
import com.kashyapmedia.firefunction.Annotations.FirebaseFunction;
import com.kashyapmedia.firefunction.Annotations.FirebaseFunctionBody;
import com.kashyapmedia.firefunction.Models.Call;

import io.reactivex.Observable;

public interface FirebaseInterface {

    @FirebaseFunction("testFunc")
    Call<ProfileResponseBody> getProfile(@FirebaseFunctionBody SampleRequestBody bodyString);

    @FirebaseFunction("testFunc")
    Observable<ProfileResponseBody> getProfileRX(@FirebaseFunctionBody SampleRequestBody bodyString);

}

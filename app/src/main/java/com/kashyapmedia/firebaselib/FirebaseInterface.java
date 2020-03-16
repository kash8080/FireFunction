package com.kashyapmedia.firebaselib;

import com.kashyapmedia.firebaselib.Models.ProfileResponseBody;
import com.kashyapmedia.firebaselib.Models.SampleRequestBody;
import com.kashyapmedia.firefunction.Annotations.FirebaseFunction;
import com.kashyapmedia.firefunction.Annotations.FirebaseFunctionBody;
import com.kashyapmedia.firefunction.Models.Call;

public interface FirebaseInterface {

    @FirebaseFunction("testFunc")
    Call<ProfileResponseBody> getProfile(@FirebaseFunctionBody SampleRequestBody bodyString);

}

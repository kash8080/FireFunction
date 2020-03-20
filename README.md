# FireFunction
Retrofit like syntax for firebase **[callable](https://firebase.google.com/docs/functions/callable)** functions in android. This library is not for the firebase **[https](https://firebase.google.com/docs/functions/http-events)** functions as for that you can already use retrofit directly.


## Download
In you project level build.gradle file
```groovy
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
In you module level build.gradle file
```groovy
android {
  ...
  // This library requires Java 8.
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}
dependencies {
  implementation 'com.github.kash8080:FireFunction:0.2.0'
}
```
## The Why
Typically you write callable functions like this
```java
Map<String,Object> mapw=new HashMap<>();
map.put("key","value");
//add more date in the map to send with the request
FirebaseFunctions.getInstance("yourFunctionRegion")
            .getHttpsCallable("yourFunctionName")
            .call(map)
            .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                @Override
                public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        //do something with the data
                    }else{
                        //show error
                    }
                }
            });
```

As you can see, we need to first put all our data in maps and then, after the request is complete ,we again need to parse the map to get the result. This library takes care of serializing/deserializing your POJOs so you don't have to :)



## How to
create an interface class to define your api just like you do in retrofit
```java
public interface FirebaseInterface {

    @FirebaseFunction("yourFunctionName")
    @FirebaseFunctionRegion("yourFunctionRegion")
    Call<ResponseBodyModel> getData(@FirebaseFunctionBody RequestBodyModel body);
    
    @FirebaseFunction("yourFunctionName")
    @FirebaseFunctionRegion("yourFunctionRegion")
    Observable<ProfileResponseBody> getDataRx(@FirebaseFunctionBody RequestBodyModel body);

}
```
Now call the api from anywhere in your code like this
```java
RequestBodyModel requestBody=new RequestBodyModel("testid","testname");
FirebaseInterface myInterface= FireFunction.getInstance().create(FirebaseInterface.class);
Call<ResponseBodyModel> data = myInterface.getData(requestBody);
data.execute(new Response<ResponseBodyModel>() {
    @Override
    public void onSuccess(ResponseBodyModel data) {
        Log.d(TAG, "onSuccess: ");
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
});

//or with observables like this
myInterface.getDataRx(requestBody)
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

```

## Contribution
Want to contribute? Feel free to submit a PR or contact me if you have any feature request.

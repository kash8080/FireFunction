package com.kashyapmedia.firefunction;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kashyapmedia.firefunction.Models.Call;
import com.kashyapmedia.firefunction.Models.Request;
import com.kashyapmedia.firefunction.Models.Response;
import com.kashyapmedia.firefunction.responses.ApiErrorResponse;
import com.kashyapmedia.firefunction.responses.ApiResponse;
import com.kashyapmedia.firefunction.responses.ApiSuccessResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import io.reactivex.Observable;

public class FireFunction {

    private static final String TAG = "FireFunction";
    public static boolean debug=false;

    private static FireFunction instance;

    private Gson gson;

    private GlobalRequestListener globalRequestListener;

    private FireFunction() {
        gson=new GsonBuilder().create();
    }

    public static FireFunction getInstance() {
        if(instance==null){
            instance=new FireFunction();
        }
        return instance;
    }

    public FireFunction setGlobalRequestListener(GlobalRequestListener globalRequestListener) {
        this.globalRequestListener = globalRequestListener;
        return this;
    }

    @SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
    public <T> T create(final Class<T> service) {
        Utils.validateServiceInterface(service);

        return (T) Proxy.newProxyInstance(service.getClassLoader(),new Class[]{service} ,new InvocationHandler() {

            @Override public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
                Object result = null;
                // If the method is a method from Object then defer to normal invocation.
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }

                result = handleFunction(proxy,method,args);

                return result;
            }
        });
    }

    private Object handleFunction(Object proxy, Method method, @Nullable Object[] args){
        final Request request=RequestParser.parseRequest(proxy,method,args);
        if(request.getReturnEnclosingClass()==Call.class){
            return handleFunctionCallback(request);
        }else if(request.getReturnEnclosingClass()== Observable.class){
            return handleFunctionObservable(request);
        }else if(request.getReturnEnclosingClass()== LiveData.class){
            return handleFunctionLiveData(request);
        }else{
            throw new RuntimeException("Invalid return type");
        }

    }

    private <P> Call<P> handleFunctionCallback(final Request request){

        if(request!=null){
            return new Call<P>() {
                @Override
                public void execute(@Nullable Response<P> response) {
                    callFirebaseFunction(request,response);
                }
            };
        }
        return null;
    }

    private <P> Observable<P> handleFunctionObservable(final Request request){

        if(request!=null){
            return Observable.create(emitter -> {
                callFirebaseFunction(request, new Response<P>() {
                    @Override
                    public void onSuccess(P data) {
                        if(!emitter.isDisposed()){
                            emitter.onNext(data);
                            emitter.onComplete();
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        if(!emitter.isDisposed()){
                            emitter.onError(e);
                        }
                    }
                });
                emitter.setCancellable(() -> {
                    // TODO: 21-03-2020 cancel firebase request
                });

            });
        }
        return null;
    }

    private <P> LiveData<ApiResponse<P>> handleFunctionLiveData(final Request request){

        if(request!=null){
            MutableLiveData<ApiResponse<P>> livedata=new MutableLiveData<>();
            callFirebaseFunction(request, new Response<P>() {
                @Override
                public void onSuccess(P data) {
                    livedata.setValue(new ApiSuccessResponse<>(data));
                }
                @Override
                public void onError(Exception e) {
                    livedata.postValue(new ApiErrorResponse(e.getMessage()));
                }
            });

            return livedata;
        }
        return null;
    }
    private <P> void callFirebaseFunction(final Request request,@Nullable  final Response<P> response){
        HashMap<String,Object> map=new HashMap<>();
        String bodyJson="";
        if(request.getBodyType()!=null && request.getBody()!=null){
            bodyJson = gson.toJson(request.getBody());
            map=gson.fromJson(bodyJson,new TypeToken<HashMap<String, Object>>() {}.getType());
        }
        if(debug){
            Log.d(TAG, request.getFunctionName()+" - request : "+bodyJson);
        }
        String functionRegion=request.getFunctionRegion();
        FirebaseFunctions mFunctions = functionRegion!=null? FirebaseFunctions.getInstance(functionRegion):FirebaseFunctions.getInstance();
        HashMap<String, Object> finalMap = map;
        mFunctions
                .getHttpsCallable(request.getFunctionName())
                .call(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> result =new HashMap<>();
                        try{
                            result = (HashMap<String, Object>) task.getResult().getData();
                        }catch (Exception e){
                            if(debug){
                                Log.e(TAG, request.getFunctionName()+ " : ",e );
                            }
                        }
                        String json=gson.toJson(result);
                        if(debug){
                            Log.d(TAG, request.getFunctionName()+" - response : "+json);
                        }
                        if(globalRequestListener !=null){
                            globalRequestListener.onSuccess(request.getFunctionName(),result);
                        }
                        if(response!=null){
                            P successData=gson.fromJson(json,request.getReturnType());
                            response.onSuccess(successData);
                        }
                    }else{
                        if(globalRequestListener !=null){
                            globalRequestListener.onError(request.getFunctionName(), finalMap,task.getException());
                        }
                        if(response!=null){
                            response.onError(task.getException());
                        }
                    }
                });

    }


    public interface GlobalRequestListener {
        void onSuccess(String functionName, HashMap<String, Object> resultData);
        void onError(String functionName,HashMap<String,Object> requestData, Exception e);
    }


}


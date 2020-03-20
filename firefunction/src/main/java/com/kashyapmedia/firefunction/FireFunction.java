package com.kashyapmedia.firefunction;

import androidx.annotation.Nullable;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kashyapmedia.firefunction.Models.Call;
import com.kashyapmedia.firefunction.Models.Request;
import com.kashyapmedia.firefunction.Models.Response;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import io.reactivex.Observable;

public class FireFunction {

    private static FireFunction instance;

    private Gson gson;

    private FireFunction() {
        gson=new GsonBuilder().create();
    }

    public static FireFunction getInstance() {
        if(instance==null){
            instance=new FireFunction();
        }
        return instance;
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
        }else{
            throw new RuntimeException("Invalid return type");
        }

    }

    private <P> Call<P> handleFunctionCallback(final Request request){

        if(request!=null){
            return new Call<P>() {
                @Override
                public void execute(Response<P> response) {
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

    private <P> void callFirebaseFunction(final Request request, final Response<P> response){
        HashMap<String,Object> map=new HashMap<>();
        if(request.getBodyType()!=null && request.getBody()!=null){
            String json = gson.toJson(request.getBody());
            map=gson.fromJson(json,new TypeToken<HashMap<String, Object>>() {}.getType());
        }

        String functionRegion=request.getFunctionRegion();
        FirebaseFunctions mFunctions = functionRegion!=null? FirebaseFunctions.getInstance(functionRegion):FirebaseFunctions.getInstance();
        mFunctions
                .getHttpsCallable(request.getFunctionName())
                .call(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                        String json=gson.toJson(result);
                        response.onSuccess((P)gson.fromJson(json,request.getReturnType()));
                    }else{
                        response.onError(task.getException());
                    }
                });

    }


}


package com.kashyapmedia.firefunction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
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

                result = handleFunctions(proxy,method,args);

                return result;
            }
        });
    }

    private <P> Call<P> handleFunctions(Object proxy, Method method, @Nullable Object[] args){

        final Request request=RequestParser.parseRequest(proxy,method,args);
        if(request!=null){
            if(request.getReturnEnclosingClass()!=Call.class){
                throw new RuntimeException("Invalid return type");
            }
            return new Call<P>() {
                @Override
                public void execute(Response<P> response) {
                    callFirebaseFunction(request,response);
                }
            };
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
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getData();
                            String json=gson.toJson(result);
                            P p=(P)gson.fromJson(json,request.getReturnType());
                            response.onSuccess(p);
                        }else{
                            response.onError(task.getException());
                        }
                    }
                });

    }


}


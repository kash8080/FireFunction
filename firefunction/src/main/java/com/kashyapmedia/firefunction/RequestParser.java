package com.kashyapmedia.firefunction;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.kashyapmedia.firefunction.Annotations.FirebaseFunction;
import com.kashyapmedia.firefunction.Annotations.FirebaseFunctionBody;
import com.kashyapmedia.firefunction.Annotations.FirebaseFunctionRegion;
import com.kashyapmedia.firefunction.Models.Request;
import com.kashyapmedia.firefunction.responses.ApiResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RequestParser {
    private static final String TAG = "RequestParser";

    public static Request parseRequest(Object proxy, Method method, @Nullable Object[] args){

        FirebaseFunction firebaseFunction=method.getAnnotation(FirebaseFunction.class);
        FirebaseFunctionRegion firebaseFunctionRegion=method.getAnnotation(FirebaseFunctionRegion.class);

        if(firebaseFunction!=null){

            Request request=new Request();
            Type returnType = (((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0]);

            request.setReturnEnclosingClass(method.getReturnType());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                //Log.d(TAG, "parseRequest: returnType:"+returnType.getTypeName());
            }
            if (request.getReturnEnclosingClass()== LiveData.class) {

                if (!(returnType instanceof ParameterizedType)) {
                    throw new IllegalArgumentException("resource must be parameterized");
                }
                Type typeWithoutGenerics=((ParameterizedType) returnType).getRawType();

                if (typeWithoutGenerics != ApiResponse.class) {
                    throw new IllegalArgumentException("type must be a resource");
                }
                Type innerReturnType = ((ParameterizedType)returnType).getActualTypeArguments()[0];
                request.setReturnType(innerReturnType);
            }else{
                request.setReturnType(returnType);
            }


            //String methodName = method.getName();//not needed
            request.setFunctionName(firebaseFunction.value());
            request.setFunctionRegion(firebaseFunctionRegion!=null?firebaseFunctionRegion.value(): null);

            Class[] parameterTypes=method.getParameterTypes();
            Annotation[][] parameterAnnotations=method.getParameterAnnotations();
            Class parameter;
            for(int i=0;i<parameterAnnotations.length;i++){
                Annotation[] annotations = parameterAnnotations[i];
                parameter = parameterTypes[i];
                for(Annotation annotation:annotations){
                    if(annotation.annotationType()== FirebaseFunctionBody.class){
                        request.setBody(args[i]);
                        request.setBodyType(parameter);
                    }
                }
            }

            return request;
        }
        return null;
    }



}

package com.kashyapmedia.firefunction;

import androidx.annotation.Nullable;

import com.kashyapmedia.firefunction.Annotations.FirebaseFunction;
import com.kashyapmedia.firefunction.Annotations.FirebaseFunctionBody;
import com.kashyapmedia.firefunction.Annotations.FirebaseFunctionRegion;
import com.kashyapmedia.firefunction.Models.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RequestParser {


    public static Request parseRequest(Object proxy, Method method, @Nullable Object[] args){

        FirebaseFunction firebaseFunction=method.getAnnotation(FirebaseFunction.class);
        FirebaseFunctionRegion firebaseFunctionRegion=method.getAnnotation(FirebaseFunctionRegion.class);

        if(firebaseFunction!=null){

            Request request=new Request();
            Type returnType = (((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0]);
            request.setReturnType(returnType);
            request.setReturnEnclosingClass(method.getReturnType());


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

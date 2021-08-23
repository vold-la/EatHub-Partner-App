package com.alam.eathub_staff.Retrofit;


import com.alam.eathub_staff.Model.FCMResponse;
import com.alam.eathub_staff.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAzxLii-g:APA91bFJ-5BNVRXgskzZDI8LDmexSic59JmxQb9DiFtyrnKkcIrOXPYaoc1jPoexo5SZr-T5kMWQqsOC5CzQtLnfD-JV2NoHIzCFjPgOdzZGXqh3Oj50eo4Ma3QFV2YWrHpQIvG-NMMn"
    })

    @POST("from/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}

package com.distractic;

import com.distractic.models.ServerRequest;
import com.distractic.models.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequestInterface {

    @POST("api/db/")
    Call<ServerResponse> operation(@Body ServerRequest request);

}
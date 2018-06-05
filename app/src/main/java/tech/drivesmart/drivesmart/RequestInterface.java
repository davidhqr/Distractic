package tech.drivesmart.drivesmart;

import tech.drivesmart.drivesmart.models.ServerRequest;
import tech.drivesmart.drivesmart.models.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequestInterface {

    @POST("api/db/")
    Call<ServerResponse> operation(@Body ServerRequest request);

}
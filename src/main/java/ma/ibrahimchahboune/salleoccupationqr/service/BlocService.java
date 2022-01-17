package ma.ibrahimchahboune.salleoccupationqr.service;

import java.util.List;

import ma.ibrahimchahboune.salleoccupationqr.beans.Bloc;
import retrofit2.Call;
import retrofit2.http.GET;

public interface BlocService {

    @GET("/api/blocs/")
    Call<List<Bloc>>  getAllBlocs();
}

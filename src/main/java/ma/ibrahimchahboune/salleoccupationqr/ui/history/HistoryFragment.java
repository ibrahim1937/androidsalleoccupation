package ma.ibrahimchahboune.salleoccupationqr.ui.history;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import ma.ibrahimchahboune.salleoccupationqr.R;
import ma.ibrahimchahboune.salleoccupationqr.adapter.OccupationAdapter;
import ma.ibrahimchahboune.salleoccupationqr.beans.Bloc;
import ma.ibrahimchahboune.salleoccupationqr.beans.Creneau;
import ma.ibrahimchahboune.salleoccupationqr.beans.Occupation;
import ma.ibrahimchahboune.salleoccupationqr.beans.Salle;
import ma.ibrahimchahboune.salleoccupationqr.beans.User;


public class HistoryFragment extends Fragment {


    private RequestQueue requestQueue;
    private ArrayList<Occupation> occupations = new ArrayList<Occupation>();
    private OccupationAdapter occupationAdapter;
    private ListView list;
    private EditText dateEdit;
    DatePickerDialog.OnDateSetListener onDateSetListener;
    private int year, month, day;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root   = inflater.inflate(R.layout.fragment_history, container, false);
        list = root.findViewById(R.id.occ_history_list);

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        dateEdit = root.findViewById(R.id.editTextDate);
        dateEdit.setText(year+"/"+(month + 1) + "/" + day);

        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        onDateSetListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();

            }
        });

        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                i1 = i1 + 1;
                String date = i+"/"+i1+"/"+i2;
                Log.d("TAG", "onDateSet: " + date);
                year = i;
                month = i1 - 1;
                day = i2;
                fetchData();
                dateEdit.setText(date);
            }
        };

        requestQueue = Volley.newRequestQueue(getContext());

        fetchData();

        return root;
    }



    public void  fetchData(){
        occupations = new ArrayList<Occupation>();
        Log.d("TAG", "fetchData: "  + convertToUrlDate(year, month, day));
        String url = getResources().getString(R.string.endpoint) + "api/occupations/" + convertToUrlDate(year, month, day);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("TAG", "onResponse: " + response.toString());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Occupation occupation = new Occupation();
                        Creneau creneau = new Creneau();
                        Salle salle = new Salle();
                        User user = new User();
                        JSONObject occupationObject = response.getJSONObject(i);
                        occupation.set_id(occupationObject.getString("_id"));
                        occupation.setCreatedAt(displayDate(occupationObject.getString("createdAt")));
                        occupation.setUpdatedAt(occupationObject.getString("updatedAt"));

                        JSONObject creneauObject = occupationObject.getJSONObject("creneau");
                        creneau.set_id(creneauObject.getString("_id"));
                        creneau.setStartTime(creneauObject.getString("startTime"));
                        creneau.setEndTime(creneauObject.getString("endTime"));

                        JSONObject salleObject = occupationObject.getJSONObject("salle");
                        salle.set_id(salleObject.getString("_id"));
                        salle.setName(salleObject.getString("name"));
                        salle.setType(salleObject.getString("type"));
                        Bloc bloc = new Bloc();
                        bloc.set_id(salleObject.getString("bloc"));
                        salle.setBloc(bloc);

                        JSONObject userObject = occupationObject.getJSONObject("user");
                        user.setId(userObject.getString("_id"));
                        user.setFirstName(userObject.getString("firstName"));
                        user.setSecondName(userObject.getString("secondName"));
                        user.setEmail(userObject.getString("email"));


                        occupation.setSalle(salle);
                        occupation.setCreneau(creneau);
                        occupation.setUser(user);
                        occupations.add(occupation);





                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                occupationAdapter = new OccupationAdapter(getActivity(), occupations);
                occupationAdapter.notifyDataSetChanged();
                list.setAdapter(occupationAdapter);

                Log.d("TAG", "onResponse: test " + occupations);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: test" + error.getMessage());
            }
        });

        requestQueue.add(jsonArrayRequest);
    }


    public String convertToUrlDate(int year, int month, int day){
        return year+"-"+(month + 1)+"-"+day;
    }

    public String displayDate(String initialDate){
        String[] split1 = initialDate.split("T");
        String[] split2 = split1[0].split("-");
        return split2[2] + "/" + split2[1] + "/" + split2[0];
    }
}
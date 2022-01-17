package ma.ibrahimchahboune.salleoccupationqr.ui.booksallecreneau;

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.service.autofill.UserData;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.socket.client.IO;
import io.socket.client.Socket;
import ma.ibrahimchahboune.salleoccupationqr.R;
import ma.ibrahimchahboune.salleoccupationqr.adapter.GridAdapter;
import ma.ibrahimchahboune.salleoccupationqr.beans.Bloc;
import ma.ibrahimchahboune.salleoccupationqr.beans.Creneau;
import ma.ibrahimchahboune.salleoccupationqr.beans.Occupation;
import ma.ibrahimchahboune.salleoccupationqr.beans.Salle;
import ma.ibrahimchahboune.salleoccupationqr.beans.User;
import ma.ibrahimchahboune.salleoccupationqr.utils.DataHolder;
import ma.ibrahimchahboune.salleoccupationqr.utils.UserGetter;


public class bookSalleByCreneau extends Fragment {


    private Salle salle;
    private Creneau creneau;
    private Bloc bloc;
    private RequestQueue requestQueue;
    private Spinner bloc_spinner, salle_spinner, creneau_spinner;
    private ArrayList<Bloc> blocs = new ArrayList<Bloc>();
    private ArrayList<Salle> salles = new ArrayList<Salle>();
    private ArrayList<Creneau> creneaux = new ArrayList<Creneau>();
    private String url;
    private User user = new User();
    private Occupation occupation;
    private Socket mSocket;

    private TextView b_name, s_name, s_type, c_field , isBooked;
    private Button bookButton, deleteButton;
    private LinearLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_book_salle_by_creneau, container, false);

        requestQueue = Volley.newRequestQueue(getContext());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("Occupation Notification", "Occupation Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        try {
            mSocket = IO.socket(
                    getResources().getString(R.string.endpoint)
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket.connect();
        mSocket.on("refreshOccupation", args -> {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleUpdate();
                    Log.d("TAG", "run:  socket" );
                }
            });
        });

        // Set user

        user = DataHolder.getInstance().getUser();


        url = getResources().getString(R.string.endpoint) + "api/";
        // Set spinner
        bloc_spinner = root.findViewById(R.id.bloc_spinner);
        salle_spinner = root.findViewById(R.id.salle_spinner);
        creneau_spinner = root.findViewById(R.id.creneau_spinner);

        // set Textfields
        b_name = root.findViewById(R.id.b_name);
        s_name = root.findViewById(R.id.s_name);
        s_type = root.findViewById(R.id.s_type);
        c_field = root.findViewById(R.id.c_field);
        isBooked  = root.findViewById(R.id.isbooked);
        bookButton = root.findViewById(R.id.booking_button);
        deleteButton = root.findViewById(R.id.delete_button);
        layout = root.findViewById(R.id.layout);




        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                postOccupation(creneau, user, salle);

            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(occupation == null){
                    new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Error while deleting the occupation").show();
                    return;
                }
                if(!occupation.getUser().getId().equals(user.getId())){
                    new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("You don't have the permission to delete").show();
                    return;
                }



                deleteOccupation(occupation.get_id());


            }
        });


        getAllCreneaux();
        fetchBlocs();


        // Array adapter



        return root;
    }

    private void deleteOccupation(String id) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.DELETE, url + "occupations/" + id, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) {
                new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Occupation Deleted")
                        .setContentText("Occupation Deleted successfully!")
                        .show();
                occupation =null;
                SendDeleteNotification("The occupation has been deleted successfully!",
                        "Delete Occupation");
                searchData(creneau,salle);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText(error.getMessage())
                        .show();
            }
        });

        requestQueue.add(jsonRequest);
    }

    private void postOccupation(Creneau creneau, User user, Salle salle) {
        Map<String, String> params = new HashMap();
        params.put("salle", salle.get_id());
        params.put("creneau", creneau.get_id());
        params.put("user", user.getId());

        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url + "occupations/bycreneau", parameters, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) {


                try {
                    String status = response.getString("status");
                    if(status.equals("success")){
                        searchData(creneau,salle);

                        JSONObject jsonObject = response.getJSONObject("occupation");
                        JSONObject creneauRe = jsonObject.getJSONObject("creneau");
                        JSONObject salleRe = jsonObject.getJSONObject("salle");

                        Bloc bloc = new Bloc();

                        Creneau creneau = new Creneau();
                        creneau.set_id(creneauRe.getString("_id"));
                        creneau.setStartTime(creneauRe.getString("startTime"));
                        creneau.setEndTime(creneauRe.getString("endTime"));

                        Salle salle = new Salle();
                        salle.set_id(salleRe.getString("_id"));
                        salle.setName(salleRe.getString("name"));
                        salle.setType(salleRe.getString("type"));
                        bloc.set_id(salleRe.getString("bloc"));
                        salle.setBloc(bloc);

                        String date = jsonObject.getString("createdAt");

                        String msg = showSuccessMessage(creneau,salle,date);
                        SendNotification("The Occupation for The Salle " + salle.getName() + " has been booked successfully for the creneau " + creneau.getStartTime() + " - " + creneau.getEndTime(), "Occupation Created");
                        new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Occupation Created")
                                .setContentText(msg)
                                .show();
                    }else {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText(response.getString("message"))
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                layout.setVisibility(View.GONE);
                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText(error.getMessage())
                        .show();
            }
        });

        requestQueue.add(jsonRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String showSuccessMessage(Creneau creneau, Salle salle, String date) {
        String creneauTime = creneau.getStartTime() + " - " + creneau.getEndTime();

        // Salle related properties

        String name = salle.getName();

        String type = salle.getType();

        String result = "";

        result += "Salle Name : " + name + "\n";
        result += "Salle Type : " + type + "\n";
        result += "Creneau : " + creneauTime + "\n";
        result += "Occupation Date : " + convertToReadableDate(date) + "\n";



        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String convertToReadableDate(String mydate){

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss", Locale.ENGLISH);
        LocalDateTime date = LocalDateTime.parse(mydate, inputFormatter);
        String formattedDate = outputFormatter.format(date);

        return formattedDate;
    }

    private void searchData(Creneau creneau, Salle _salle) {
        occupation = null;
        Map<String, String> params = new HashMap();
        params.put("salle", _salle.get_id());
        params.put("creneau", creneau.get_id());

        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url + "salles/available", parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //TODO: handle success
                Salle mysalle = new Salle();
                try {
                    JSONObject salleObject = response.getJSONObject("salle");
                    Bloc _bloc = new Bloc();
                    _bloc.set_id(salleObject.getString("bloc"));
                    mysalle.setBloc(_bloc);
                    mysalle.setName(salleObject.getString("name"));
                    mysalle.set_id(salleObject.getString("_id"));
                    mysalle.setType(salleObject.getString("type"));
                    mysalle.setBooked(response.getBoolean("isBooked"));

                    if(response.getBoolean("isBooked")){
                        JSONObject occupationObject = response.getJSONObject("occupation");
                        Occupation _occupation  = new Occupation();
                        _occupation.setSalle(mysalle);
                        _occupation.setCreneau(creneau);
                        _occupation.set_id(occupationObject.getString("_id"));
                        User _user = new User();
                        _user.setId(occupationObject.getString("user"));
                        _occupation.setUser(_user);

                        occupation = _occupation;
                    }

                    salle = mysalle;

                    layout.setVisibility(View.VISIBLE);
                    updateData(bloc,salle,creneau);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                layout.setVisibility(View.GONE);
                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText(error.getMessage())
                        .show();
            }
        });

        requestQueue.add(jsonRequest);

    }


    public void fetchBlocs(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url + "blocs", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("TAG", "onResponse: " + response.toString());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Bloc bloc = new Bloc();
                        JSONObject jsonObject = response.getJSONObject(i);
                        bloc.set_id(jsonObject.getString("_id"));
                        bloc.setName(jsonObject.getString("name"));
                        blocs.add(bloc);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("TAG", "onResponse: " + blocs.toString());
                ArrayAdapter<Bloc> adapter = new ArrayAdapter<Bloc>(getContext(), android.R.layout.simple_spinner_dropdown_item, blocs);
                bloc_spinner.setAdapter(adapter);
                bloc_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        bloc = blocs.get(i);
                        getAllSalles(bloc);
                        occupation = null;
                        handleUpdate();




                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: " + error.getMessage());
            }
        });

        requestQueue.add(jsonArrayRequest);
    }


    public void getAllCreneaux(){
        creneaux = new ArrayList<Creneau>();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url + "creneaux", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("TAG", "onResponse: getCreneaux " + response.toString());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Creneau creneau = new Creneau();
                        JSONObject jsonObject = response.getJSONObject(i);

                        creneau.setStartTime(jsonObject.getString("startTime"));
                        creneau.setEndTime(jsonObject.getString("endTime"));
                        creneau.set_id(jsonObject.getString("_id"));

                        int end_hour = Integer.parseInt(creneau.getEndTime().split(":")[0]);
                        int edn_minute = Integer.parseInt(creneau.getEndTime().split(":")[1]);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            int hour = LocalDateTime.now().getHour();
                            int minute = LocalDateTime.now().getMinute();
                            Log.d("TAG", "onResponse: " + hour);
                            if(hour >= end_hour && minute >= edn_minute){
                                Log.d("TAG", "onResponse: not ok" );
                                continue;
                            }
                            creneaux.add(creneau);
                        }




                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                ArrayAdapter<Creneau> adapter = new ArrayAdapter<Creneau>(getContext(), android.R.layout.simple_spinner_dropdown_item, creneaux);
                adapter.notifyDataSetChanged();
                creneau_spinner.setAdapter(adapter);
                creneau_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        creneau = creneaux.get(i);
                        occupation = null;
                        handleUpdate();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: " + error.getMessage());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }


    public void getAllSalles(Bloc bloc){
        salles = new ArrayList<Salle>();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url + "salles/findbybloc/" + bloc.get_id() , null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("TAG", "onResponse: getsalles " + response.toString());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Salle salle = new Salle();
                        JSONObject jsonObject = response.getJSONObject(i);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("salle");
                        salle.setBooked(jsonObject.getBoolean("isBooked"));
                        salle.setBloc(bloc);
                        salle.setType(jsonObject1.getString("type"));
                        salle.setName(jsonObject1.getString("name"));
                        salle.set_id(jsonObject1.getString("_id"));
                        salles.add(salle);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                ArrayAdapter<Salle> adapter = new ArrayAdapter<Salle>(getContext(), android.R.layout.simple_spinner_dropdown_item, salles);
                adapter.notifyDataSetChanged();
                salle_spinner.setAdapter(adapter);
                salle_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        salle = salles.get(i);
                        occupation = null;
                        handleUpdate();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: " + error.getMessage());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }


    public void handleUpdate(){
        if(creneau == null || salle == null || bloc == null){
            return;
        }
        searchData(creneau,salle);
    }



    public void updateData(Bloc bloc, Salle salle, Creneau creneau){

        b_name.setText(bloc.getName());
        s_name.setText(salle.getName());
        s_type.setText(salle.getType());
        c_field.setText(creneau.getStartTime() + " - " + creneau.getEndTime());

        boolean booked = salle.isBooked();

        isBooked.setText(booked ? "false" : "true");
        isBooked.setTextColor(booked ? Color.RED : Color.GREEN);

        bookButton.setVisibility(booked ? View.GONE : View.VISIBLE);

        if(occupation == null){
            deleteButton.setVisibility(View.GONE);
        }else if(!occupation.getUser().getId().equals(user.getId())) {
            deleteButton.setVisibility(View.GONE);
        }else{
            deleteButton.setVisibility(View.VISIBLE);
        }







    }

    private void SendNotification(String message, String title){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "Occupation Notification");
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.ic_baseline_bookmark_added_24);
        builder.setAutoCancel(true);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getContext());
        managerCompat.notify(1, builder.build());


    }
    private void SendDeleteNotification(String message, String title){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "Occupation Notification");
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.ic_baseline_delete_24);
        builder.setAutoCancel(true);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getContext());
        managerCompat.notify(1, builder.build());


    }


}
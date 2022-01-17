package ma.ibrahimchahboune.salleoccupationqr.ui.gallery;

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ma.ibrahimchahboune.salleoccupationqr.MainActivity;
import ma.ibrahimchahboune.salleoccupationqr.R;
import ma.ibrahimchahboune.salleoccupationqr.beans.Bloc;
import ma.ibrahimchahboune.salleoccupationqr.beans.Creneau;
import ma.ibrahimchahboune.salleoccupationqr.beans.Salle;
import ma.ibrahimchahboune.salleoccupationqr.beans.User;
import ma.ibrahimchahboune.salleoccupationqr.databinding.FragmentGalleryBinding;
import ma.ibrahimchahboune.salleoccupationqr.utils.DataHolder;

public class GalleryFragment extends Fragment {


    private FragmentGalleryBinding binding;
    private CodeScanner mCodeScanner;
    private RequestQueue requestQueue;
    private User user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("Occupation Notification", "Occupation Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        requestQueue = Volley.newRequestQueue(getContext());

//        // Set user
//        SharedPreferences prefs = getActivity().getSharedPreferences("user", MODE_PRIVATE);
//        user.setEmail(prefs.getString("email", null));
//        user.setFirstName(prefs.getString("firstName", null));
//        user.setSecondName(prefs.getString("secondName", null));
//        user.setId(prefs.getString("id", null));

        user = DataHolder.getInstance().getUser();

        CodeScannerView scannerView = root.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(getContext(), scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Creating the get param


                        // we are getting the results
                        String url = getResources().getString(R.string.endpoint).toString() + "api/occupations";
                        fetchData(url, result.getText());
//                        Toast.makeText(getContext(), result.getText(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }


    public  void fetchData(String url, String _id){
        Map<String, String> mymap = new HashMap<>();
        mymap.put("salle", _id);
        mymap.put("user", user.getId());
//        Log.d("Salleid", "fetchData: " + idSalle);
        JSONObject jsonObject = new JSONObject(mymap);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url , jsonObject, new Response.Listener<JSONObject>() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", "onResponse: " + response.toString());
//                        Toast.makeText(getContext(), response.toString(), Toast.LENGTH_SHORT).show();
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");
                            if(status.equals("failure")){
                                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Occupation Error")
                                        .setContentText(message)
                                        .show();
                            }else if(status.equals("error")){
                                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Error")
                                        .setContentText(message)
                                        .show();

                            }else if(status.equals("success")){
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
                                        .setTitleText("Occupation Created!")
                                        .setContentText(msg)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", "onErrorResponse: " + error.getMessage());
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(error.getMessage())
                                .setContentText(error.getMessage())
                                .show();
                    }
                }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String json = new String(
                            response.data,
                            "UTF-8"
                    );

                    if (json.length() == 0) {
                        return Response.success(
                                null,
                                HttpHeaderParser.parseCacheHeaders(response)
                        );
                    }
                    else {
                        return super.parseNetworkResponse(response);
                    }
                }
                catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }

        };

        requestQueue.add(jsonObjectRequest);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private String showSuccessMessage(Creneau creneau, Salle salle, String data){

//        // Convert Javascript Date to java date
//        long datelong =  Date.parse(data);
////        Date date = new Date(datelong);
////        String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
//        DateFormat jsfmt = new SimpleDateFormat("EE MMM d y H:m:s 'GMT'Z (zz)");
//        Date date = new Date();
//        try {
//            date = jsfmt.parse(data);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        // getTime from creneau

        String creneauTime = creneau.getStartTime() + " - " + creneau.getEndTime();

        // Salle related properties

        String name = salle.getName();

        String type = salle.getType();

        String result = "";

        result += "Salle Name : " + name + "\n";
        result += "Salle Type : " + type + "\n";
        result += "Creneau : " + creneauTime + "\n";
        result += "Occupation Date : " + convertToReadableDate(data) + "\n";



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


}
package ma.ibrahimchahboune.salleoccupationqr.ui.home;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import ma.ibrahimchahboune.salleoccupationqr.R;
import ma.ibrahimchahboune.salleoccupationqr.adapter.GridAdapter;
import ma.ibrahimchahboune.salleoccupationqr.beans.Bloc;
import ma.ibrahimchahboune.salleoccupationqr.beans.Salle;
import ma.ibrahimchahboune.salleoccupationqr.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {


    private FragmentHomeBinding binding;
    private RequestQueue requestQueue;
    private Spinner spinner;
    private ArrayList<Bloc> items = new ArrayList<Bloc>();
    private ArrayList<Salle> salles = new ArrayList<Salle>();
    private String url;
    private GridView gridView;
    private Bloc mybloc;
    private Socket mSocket;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        requestQueue = Volley.newRequestQueue(getContext());

        spinner = root.findViewById(R.id.spinner);

        gridView = root.findViewById(R.id.gridView);

        url = getResources().getString(R.string.endpoint) + "api/blocs";

        try {
            mSocket = IO.socket(
                    getResources().getString(R.string.endpoint)
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);

        fetchData(url);





        return root;
    }



    private void fetchData(String url){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("TAG", "onResponse: " + response.toString());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Bloc bloc = new Bloc();
                        JSONObject jsonObject = response.getJSONObject(i);
                        bloc.set_id(jsonObject.getString("_id"));
                        bloc.setName(jsonObject.getString("name"));
                        items.add(bloc);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("TAG", "onResponse: " + items.toString());
                ArrayAdapter<Bloc> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
                adapter.notifyDataSetChanged();
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        Bloc bloc = items.get(i);
                        mybloc = bloc;
                        getAllSalles(bloc);
                        mSocket.connect();
                        mSocket.on("refreshSalles", args -> {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < args.length; i++) {
                                        String blocId = args[i].toString();
                                        if(bloc.get_id().equals(blocId)){
                                            getAllSalles(bloc);
                                        }
                                    }


                                }
                            });
                        });

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
        String url = getResources().getString(R.string.endpoint) + "api/salles/findbybloc/" + bloc.get_id();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
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

                GridAdapter gridAdapter = new GridAdapter(getContext(), bloc, salles);
                gridAdapter.notifyDataSetChanged();
                gridView.setAdapter(gridAdapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: " + error.getMessage());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("refreshSalles");
    }


}
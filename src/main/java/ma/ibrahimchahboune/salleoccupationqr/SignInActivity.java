package ma.ibrahimchahboune.salleoccupationqr;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ma.ibrahimchahboune.salleoccupationqr.beans.Bloc;
import ma.ibrahimchahboune.salleoccupationqr.beans.Creneau;
import ma.ibrahimchahboune.salleoccupationqr.beans.Salle;
import ma.ibrahimchahboune.salleoccupationqr.beans.User;
import ma.ibrahimchahboune.salleoccupationqr.utils.DataHolder;

public class SignInActivity extends AppCompatActivity {


    private RequestQueue requestQueue;
    private Button btn;
    private EditText email, password;
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        requestQueue = Volley.newRequestQueue(this);


        btn = findViewById(R.id.signin);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        baseUrl = getResources().getString(R.string.endpoint);


        btn.setOnClickListener(view -> {

            // check if the email is not null
            if(email.getText().toString().matches("")){
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("Email Not Set").show();
                return;

            }
            // Check if the password is not null
            if(password.getText().toString().matches("")){
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("Password Not Set").show();
                return;
            }

            String _password = password.getText().toString();
            String _email = email.getText().toString();

            signIn(_email, _password);
        });


    }

    private void signIn(String email, String password) {
        String url = baseUrl + "api/login";
        Map<String, String> mymap = new HashMap<>();
        mymap.put("email", email);
        mymap.put("password", password);
        JSONObject jsonObject = new JSONObject(mymap);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url , jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // user logged in
                        Intent intent = new Intent(SignInActivity.this, MainActivity2.class);
                        try {
                            User user = new User();
                            user.setEmail(email);
                            user.setId(response.getString("id"));
                            user.setSecondName(response.getString("secondName"));
                            user.setFirstName(response.getString("firstName"));
                            DataHolder.getInstance().setUser(user);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(intent);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        new SweetAlertDialog(SignInActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText("Wrong Credentials")
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
}
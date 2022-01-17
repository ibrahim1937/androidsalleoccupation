package ma.ibrahimchahboune.salleoccupationqr.ui.stats;

import android.graphics.DashPathEffect;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ma.ibrahimchahboune.salleoccupationqr.R;


public class StatsFragment extends Fragment {


    private RequestQueue requestQueue;
    private Cartesian cartesian;
    private Pie pie;
    private String baseUrl;
    private AnyChartView occ_perbloc_today, most_salle_booked;
    private Column column;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_stats, container, false);
        baseUrl = getResources().getString(R.string.endpoint) + "api/";
        requestQueue = Volley.newRequestQueue(getContext());


        most_salle_booked = root.findViewById(R.id.most_booked_salle);
        APIlib.getInstance().setActiveAnyChartView(most_salle_booked);
        pie = AnyChart.pie();
       fetchStats();



        occ_perbloc_today = root.findViewById(R.id.occ_perbloc_today);
        APIlib.getInstance().setActiveAnyChartView(occ_perbloc_today);
        cartesian = AnyChart.cartesian();
        fetchAll();



        return root;
    }




    public void fetchAll() {
        getOccupationPerSalleToday();
        getMostSallesOccupied();
    }

    private void getMostSallesOccupied() {
    }

    public void getOccupationPerSalleToday(){
        String url = baseUrl + "occupations/perbloc";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    APIlib.getInstance().setActiveAnyChartView(occ_perbloc_today);

                    column = cartesian.column(getData(response));

                    column.tooltip()
                            .titleFormat("{%X}")
                            .position(Position.CENTER_BOTTOM)
                            .anchor(Anchor.CENTER_BOTTOM)
                            .offsetX(0d)
                            .offsetY(5d)
                            .format("{%Value}{groupsSeparator: }");

                    cartesian.animation(true);
                    cartesian.title("Occupation Per Bloc Today");

                    cartesian.yScale().minimum(0d);

                    cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

                    cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
                    cartesian.interactivity().hoverMode(HoverMode.BY_X);

                    cartesian.xAxis(0).title("Bloc");
                    cartesian.yAxis(0).title("Occupation");


                    occ_perbloc_today.setChart(cartesian);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);

    }


    public void fetchStats(){
        String url = baseUrl + "stats/mostbokkedsalles";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    APIlib.getInstance().setActiveAnyChartView(most_salle_booked);

                    pie.data(getData(response));

                    pie.animation(true);
                    pie.labels().position("outside");
                    pie.legend().enabled(false);
                    pie.sort("asc");
                    pie.title("Most Booked Salles");

                    most_salle_booked.setChart(pie);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText(error.getMessage()).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }


    public List<DataEntry> getData(JSONObject jsonObject) throws JSONException {
        List<DataEntry> dataEntries = new ArrayList<DataEntry>();
        JSONArray labels = jsonObject.getJSONArray("labels");
        JSONArray data =  jsonObject.getJSONArray("data");

        for (int i = 0; i < labels.length(); i++) {
            dataEntries.add(new ValueDataEntry(labels.getString(i), data.getInt(i)));
        }
        return dataEntries;
    }
}
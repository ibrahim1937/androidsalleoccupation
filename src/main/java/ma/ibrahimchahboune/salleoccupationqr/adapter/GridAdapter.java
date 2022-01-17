package ma.ibrahimchahboune.salleoccupationqr.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

import ma.ibrahimchahboune.salleoccupationqr.R;
import ma.ibrahimchahboune.salleoccupationqr.beans.Bloc;
import ma.ibrahimchahboune.salleoccupationqr.beans.Salle;
import ma.ibrahimchahboune.salleoccupationqr.dialog.CustomDialogClass;

public class GridAdapter extends BaseAdapter {

    private Context context;
    private Bloc bloc;
    private ArrayList<Salle> salles;
    private LayoutInflater inflater;

    public GridAdapter(Context context, Bloc bloc, ArrayList<Salle> salle) {
        this.context = context;
        this.bloc = bloc;
        this.salles = salle;
    }

    @Override
    public int getCount() {
        return salles.size();
    }

    @Override
    public Object getItem(int i) {
        return salles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i + 1;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(inflater == null)
            inflater = (LayoutInflater)  context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(view == null)
            view = inflater.inflate(R.layout.grid_item, null);

        Salle salle = salles.get(i);

        Button button = view.findViewById(R.id.button);
        button.setText(salle.getName());
        if(salle.isBooked()){
            button.setBackgroundColor(Color.RED);

        }else {
            button.setBackgroundColor(Color.GREEN);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                ViewGroup viewGroup = view.findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.custom_dialog_salle, viewGroup, false);
                TextView salle_name, salle_type, bloc_name, salle_bokked;
                Button ok_button = dialogView.findViewById(R.id.button_ok);
                salle_name =  dialogView.findViewById(R.id.salle_name);
                salle_type = dialogView.findViewById(R.id.salle_type);
                bloc_name = dialogView.findViewById(R.id.bloc_name);
                salle_bokked = dialogView.findViewById(R.id.salle_booked);
                salle_name.setText(salle.getName());
                salle_type.setText(salle.getType());
                bloc_name.setText(salle.getBloc().getName());
                salle_bokked.setText(salle.isBooked() ? "Booked" : "Free");
                if(salle.isBooked()){
                    salle_bokked.setTextColor(Color.RED);
                }else{
                    salle_bokked.setTextColor(Color.GREEN);
                }
                builder.setView(dialogView);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                ok_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });

        return view;
    }
}

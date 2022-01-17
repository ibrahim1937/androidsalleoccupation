package ma.ibrahimchahboune.salleoccupationqr.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ma.ibrahimchahboune.salleoccupationqr.R;
import ma.ibrahimchahboune.salleoccupationqr.beans.Occupation;

public class OccupationAdapter extends BaseAdapter {

    private ArrayList<Occupation> occupations;
    private LayoutInflater inflater;

    public OccupationAdapter(Activity activity, ArrayList<Occupation> occupations) {
        this.occupations = occupations;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return occupations.size();
    }

    @Override
    public Object getItem(int i) {
        return occupations.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i + 1;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null)
            view = inflater.inflate(R.layout.occupation_history_item, null);

        TextView occ_creneau, occ_salle_name, occ_salle_type, occ_date, occ_user;

        occ_creneau = view.findViewById(R.id.occ_creneau);
        occ_salle_name = view.findViewById(R.id.occ_salle_name);
        occ_salle_type = view.findViewById(R.id.occ_salle_type);
        occ_date = view.findViewById(R.id.occ_date);
        occ_user = view.findViewById(R.id.occ_user);


        occ_creneau.setText(occupations.get(i).getCreneau().getStartTime() + " - " + occupations.get(i).getCreneau().getEndTime());

        occ_salle_name.setText(occupations.get(i).getSalle().getName());

        occ_salle_type.setText(occupations.get(i).getSalle().getType());

        occ_date.setText(occupations.get(i).getCreatedAt());

        occ_user.setText(occupations.get(i).getUser().getFirstName() + " " + occupations.get(i).getUser().getSecondName());


        return view;
    }
}

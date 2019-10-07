package com.namnoit.zalomaps;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.namnoit.zalomaps.data.PlaceModel;

import java.util.ArrayList;

public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.ViewHolder> {
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String ACTION_FOCUS = "focus";
    public static final String KEY_ID = "id";
    private ArrayList<PlaceModel> list;
    private Context context;

    public PlacesListAdapter(ArrayList<PlaceModel> list, Context context){
        this.list = list;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_place,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (!list.get(position).isChosen()){
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }
        if (position == list.size()-1){
        }
        holder.itemView.setVisibility(View.VISIBLE);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        holder.notes.setText(list.get(position).getNote());
        holder.icon.setImageResource(R.drawable.ic_marker_education);
        switch (list.get(position).getType()){
            case PlaceModel.TYPE_FOOD_DRINK:
                holder.category.setText(R.string.food_drink);
                holder.icon.setImageResource(R.drawable.ic_marker_food);
                break;
            case PlaceModel.TYPE_ENTERTAINMENT:
                holder.category.setText(R.string.entertainment);
                holder.icon.setImageResource(R.drawable.ic_marker_entertainment);
                break;
            case PlaceModel.TYPE_EDUCATION:
                holder.category.setText(R.string.education);
                holder.icon.setImageResource(R.drawable.ic_marker_education);
                break;
            case PlaceModel.TYPE_VEHICLE_REPAIR:
                holder.category.setText(R.string.vehicle_repair);
                holder.icon.setImageResource(R.drawable.ic_marker_car_repair);
                break;
            case PlaceModel.TYPE_RELIGION:
                holder.category.setText(R.string.religion);
                holder.icon.setImageResource(R.drawable.ic_marker_religion);
                break;
            case PlaceModel.TYPE_ADMINISTRATION:
                holder.category.setText(R.string.administration);
                holder.icon.setImageResource(R.drawable.ic_marker_administration);
                break;
            case PlaceModel.TYPE_GASOLINE:
                holder.category.setText(R.string.gasoline);
                holder.icon.setImageResource(R.drawable.ic_marker_gasoline);
                break;
            case PlaceModel.TYPE_OTHER:
                holder.category.setText(R.string.other);
                holder.icon.setImageResource(R.drawable.ic_marker_other);
                break;
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent broadcast = new Intent(ListActivity.BROADCAST_START_SELECTING);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(KEY_ID,list.get(position).getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView icon;
        private TextView category, notes;
        private ImageButton buttonMore;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.image_item);
            category = itemView.findViewById(R.id.text_item_category);
            notes = itemView.findViewById(R.id.text_item_note);
            buttonMore = itemView.findViewById(R.id.button_item_more);
        }
    }
}

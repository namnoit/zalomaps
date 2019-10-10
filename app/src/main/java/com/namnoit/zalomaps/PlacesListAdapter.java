package com.namnoit.zalomaps;

import android.content.Context;
import android.content.DialogInterface;
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

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.namnoit.zalomaps.data.PlaceModel;
import com.namnoit.zalomaps.data.PlacesListManager;

import java.util.ArrayList;
import java.util.Objects;

public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.ViewHolder> {
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String ACTION_FOCUS = "focus";
    public static final String KEY_ID = "id";
    public static final String KEY_SELECTED_COUNT = "selected_count";
    private static final String ITEM_POSITION = "item_position";
    private ArrayList<PlaceModel> list;
    private PlacesListManager listManager;
    private Context context;

    public PlacesListAdapter(Context context){
        this.context = context;
        listManager = PlacesListManager.getInstance(context);
        list = listManager.getPlacesList();
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
        holder.itemView.setVisibility(View.VISIBLE);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        holder.notes.setText(list.get(position).getNote());
        final PlaceModel place = list.get(position);
        int icon;
        switch (place.getType()){
            case PlaceModel.TYPE_FOOD_DRINK:
                holder.category.setText(R.string.food_drink);
                icon = R.drawable.ic_marker_food;
                break;
            case PlaceModel.TYPE_ENTERTAINMENT:
                holder.category.setText(R.string.entertainment);
                icon = R.drawable.ic_marker_entertainment;
                break;
            case PlaceModel.TYPE_EDUCATION:
                holder.category.setText(R.string.education);
                icon = R.drawable.ic_marker_education;
                break;
            case PlaceModel.TYPE_VEHICLE_REPAIR:
                holder.category.setText(R.string.vehicle_repair);
                icon = R.drawable.ic_marker_car_repair;
                break;
            case PlaceModel.TYPE_RELIGION:
                holder.category.setText(R.string.religion);
                icon = R.drawable.ic_marker_religion;
                break;
            case PlaceModel.TYPE_ADMINISTRATION:
                holder.category.setText(R.string.administration);
                icon = R.drawable.ic_marker_administration;
                break;
            case PlaceModel.TYPE_GASOLINE:
                holder.category.setText(R.string.gasoline);
                icon = R.drawable.ic_marker_gasoline;
                break;
            default:
                holder.category.setText(R.string.other);
                icon = R.drawable.ic_marker_other;
                break;
        }
        if (listManager.getSelectedCount() > 0){
//            holder.buttonMore.setEnabled(false);
            holder.icon.setImageResource(listManager.isSelected(place) ?
                    R.drawable.ic_check : R.drawable.ic_uncheck);
        }
        else {
//            holder.buttonMore.setEnabled(true);
            holder.icon.setImageResource(icon);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                select(place, position);
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listManager.getSelectedCount() > 0) {
                    select(place, position);
                }
                else {
                    Intent intent = new Intent(context, MapActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(KEY_ID, list.get(position).getId());
                    context.startActivity(intent);
                }
            }
        });
        holder.buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listManager.getSelectedCount() > 0) {
                    select(place, position);
                }
                else {
                    View infoDialog = LayoutInflater.from(context).inflate(R.layout.dialog_add_place, null);
                    final ChipGroup chipGroup = infoDialog.findViewById(R.id.chip_group_add_place);
                    final TextInputEditText textNotes = infoDialog.findViewById(R.id.text_notes_add_place);
                    final TextInputEditText textAddress = infoDialog.findViewById(R.id.text_address);
                    textAddress.setText(place.getAddress());
                    final int oldType = place.getType();
                    textNotes.setText(place.getNote());
                    switch (place.getType()) {
                        case PlaceModel.TYPE_FOOD_DRINK:
                            chipGroup.check(R.id.chip_food_drink);
                            break;
                        case PlaceModel.TYPE_ENTERTAINMENT:
                            chipGroup.check(R.id.chip_entertainment);
                            break;
                        case PlaceModel.TYPE_EDUCATION:
                            chipGroup.check(R.id.chip_education);
                            break;
                        case PlaceModel.TYPE_VEHICLE_REPAIR:
                            chipGroup.check(R.id.chip_vehicle_repair);
                            break;
                        case PlaceModel.TYPE_RELIGION:
                            chipGroup.check(R.id.chip_religion);
                            break;
                        case PlaceModel.TYPE_ADMINISTRATION:
                            chipGroup.check(R.id.chip_administration);
                            break;
                        case PlaceModel.TYPE_GASOLINE:
                            chipGroup.check(R.id.chip_gasoline);
                            break;
                        default:
                            chipGroup.check(R.id.chip_other);
                            break;
                    }
                    new MaterialAlertDialogBuilder(context, R.style.MaterialDialogStyle)
                            .setTitle(PlaceModel.getTypeInString(oldType))
                            .setView(infoDialog)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    int newType = PlaceModel.getTypeByIdDialog(chipGroup.getCheckedChipId());
                                    if (newType != oldType) {
                                        place.setType(newType);
                                    }
                                    place.setNote(Objects.requireNonNull(textNotes.getText()).toString());
                                    listManager.updatePlace(place);
                                    notifyItemChanged(position);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    listManager.delete(place);
                                    notifyItemRemoved(position);
                                }
                            })
                            .show();
                }
            }
        });
    }

    private void select(PlaceModel place, int position){
        Intent broadcast = new Intent(ListActivity.BROADCAST_SELECT);
        if (listManager.isSelected(place)){
            listManager.removeSelection(place);
            if (listManager.getSelectedCount() == 0) {
                broadcast.setAction(ListActivity.BROADCAST_FINISH_SELECTING);
                notifyDataSetChanged();
            }
        }
        else {
            listManager.select(place);
            if (listManager.getSelectedCount() == 1) {
                broadcast.setAction(ListActivity.BROADCAST_START_SELECTING);
                notifyDataSetChanged();
            }
        }
        notifyItemChanged(position);
        broadcast.putExtra(KEY_SELECTED_COUNT, listManager.getSelectedCount());
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
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

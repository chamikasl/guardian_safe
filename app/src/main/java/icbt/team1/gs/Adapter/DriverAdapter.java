package icbt.team1.gs.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import icbt.team1.gs.Model.Driver;
import icbt.team1.gs.R;
public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {
    private List<Driver> driverList;
    private int selectedItemPosition = RecyclerView.NO_POSITION; // Initialize with no selection
    private int selectedDriverPosition = RecyclerView.NO_POSITION; // Initialize with no selection
    //private ChildAdapter.OnItemClickListener onItemClickListener;
    private OnItemClickListener onItemClickListener;

    private String childId;

    public DriverAdapter(List<Driver> driverList, OnItemClickListener onItemClickListener, String childId) {
        this.driverList = driverList;
        this.onItemClickListener = onItemClickListener;
        this.childId = childId;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_parent_display_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);
        holder.driverNameTextView.setText(driver.getName());
        holder.busNumberTextView.setText(driver.getBusno());

        // Set a click listener on the item to handle selection
        holder.itemView.setOnClickListener(v -> {
            // Update the selected item position
            int previousSelectedPosition = selectedItemPosition;
            selectedItemPosition = holder.getAdapterPosition();

            // Notify data change to update UI
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedItemPosition);

            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(driver, childId);
            }
        });

        // Check if this item is the selected one and update its appearance
        boolean isSelected = position == selectedItemPosition;
        holder.itemView.setSelected(isSelected);
        holder.itemView.setBackgroundResource(isSelected ? R.drawable.selected_item_background : android.R.color.transparent);
    }

    public int getSelectedDriverPosition() {
        return selectedItemPosition;
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public Driver getSelectedDriver() {
        if (selectedItemPosition != RecyclerView.NO_POSITION) {
            return driverList.get(selectedItemPosition);
        }
        return null; // No item selected
    }

    public void setSelectedDriverPosition(int position) {
        selectedDriverPosition = position;
        notifyDataSetChanged(); // Notify the adapter of data change to reflect the selection change
    }

    public interface OnItemClickListener {
        void onItemClick(Driver driver, String childId);
    }

    public static class DriverViewHolder extends RecyclerView.ViewHolder {
        public TextView driverNameTextView;
        public TextView busNumberTextView;

        public DriverViewHolder(View itemView) {
            super(itemView);
            driverNameTextView = itemView.findViewById(R.id.driver_name);
            busNumberTextView = itemView.findViewById(R.id.bus_number);
        }
    }
}

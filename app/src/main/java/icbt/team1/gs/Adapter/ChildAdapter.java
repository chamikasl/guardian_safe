package icbt.team1.gs.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import icbt.team1.gs.Model.Child;
import icbt.team1.gs.R;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<Child> childList;
    private OnItemClickListener onItemClickListener;

    // Constructor to initialize the adapter with a list of children and an item click listener
    public ChildAdapter(List<Child> childList, OnItemClickListener onItemClickListener) {
        this.childList = childList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each child item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_parent_display_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = childList.get(position);

        // Set child data to the views in the item layout
        holder.childNameTextView.setText(child.getName());
        holder.childSclTextView.setText(child.getSchool());

        // Add click listener to handle item click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    // Interface to define the item click callback
    public interface OnItemClickListener {
        void onItemClick(Child child);
    }

    // ViewHolder class to hold the views in each child item
    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        public TextView childNameTextView;
        public TextView childSclTextView;

        public ChildViewHolder(View itemView) {
            super(itemView);
            childNameTextView = itemView.findViewById(R.id.name);
            childSclTextView = itemView.findViewById(R.id.scl);
        }
    }
}

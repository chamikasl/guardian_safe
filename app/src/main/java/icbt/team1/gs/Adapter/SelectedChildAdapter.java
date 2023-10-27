package icbt.team1.gs.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import icbt.team1.gs.Model.Child;
import icbt.team1.gs.R;

public class SelectedChildAdapter extends RecyclerView.Adapter<SelectedChildAdapter.ViewHolder> {

    private List<Child> selectedChildrenList;
    private OnAcceptRejectClickListener clickListener;

    public SelectedChildAdapter(List<Child> selectedChildrenList, OnAcceptRejectClickListener clickListener) {
        this.selectedChildrenList = selectedChildrenList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_driver_child, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Child child = selectedChildrenList.get(position);

        // Bind child's name and school to TextViews
        holder.childNameTextView.setText(child.getName());
        holder.childSchoolTextView.setText(child.getSchool());

        // Set click listeners for the accept and reject buttons
        holder.callButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCallClick(child); // Pass the clicked child to the listener
            }
        });

        holder.viewButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onViewClick(child); // Pass the clicked child to the listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedChildrenList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView childNameTextView, childSchoolTextView;
        ImageView callButton, viewButton;

        public ViewHolder(View itemView) {
            super(itemView);
            childNameTextView = itemView.findViewById(R.id.child_name);
            childSchoolTextView = itemView.findViewById(R.id.child_school);
            callButton = itemView.findViewById(R.id.call);
            viewButton = itemView.findViewById(R.id.view);
        }
    }

    // Define an interface for click listeners
    public interface OnAcceptRejectClickListener {
        void onCallClick(Child child);
        void onViewClick(Child child);
    }
}

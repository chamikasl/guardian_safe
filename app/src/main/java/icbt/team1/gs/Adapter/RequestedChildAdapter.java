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

public class RequestedChildAdapter extends RecyclerView.Adapter<RequestedChildAdapter.ViewHolder> {

    private List<Child> requestedChildrenList;
    private OnAcceptRejectClickListener clickListener;

    public RequestedChildAdapter(List<Child> requestedChildrenList, OnAcceptRejectClickListener clickListener) {
        this.requestedChildrenList = requestedChildrenList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_driver_requested_child, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Child child = requestedChildrenList.get(position);

        // Bind child's name and school to TextViews
        holder.childNameTextView.setText(child.getName());
        holder.childSchoolTextView.setText(child.getSchool());

        // Set click listeners for the accept and reject buttons
        holder.acceptButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAcceptClick(child); // Pass the clicked child to the listener
            }
        });

        holder.rejectButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRejectClick(child); // Pass the clicked child to the listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestedChildrenList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView childNameTextView, childSchoolTextView;
        ImageView acceptButton, rejectButton;

        public ViewHolder(View itemView) {
            super(itemView);
            childNameTextView = itemView.findViewById(R.id.child_name);
            childSchoolTextView = itemView.findViewById(R.id.child_school);
            acceptButton = itemView.findViewById(R.id.accept);
            rejectButton = itemView.findViewById(R.id.reject);
        }
    }

    // Define an interface for click listeners
    public interface OnAcceptRejectClickListener {
        void onAcceptClick(Child child);
        void onRejectClick(Child child);
    }
}

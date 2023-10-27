package icbt.team1.gs.Parent;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;
import icbt.team1.gs.Model.Child;

public class ChildDiffCallback extends DiffUtil.Callback {
    private final List<Child> oldList;
    private final List<Child> newList;

    public ChildDiffCallback(List<Child> oldList, List<Child> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Implement how to check if two items represent the same object
        return oldList.get(oldItemPosition).getUid().equals(newList.get(newItemPosition).getUid());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Implement how to check if the contents of two items are the same
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}

package com.example.jsonvalidator.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jsonvalidator.R;
import com.example.jsonvalidator.model.ResultItem;
import java.util.ArrayList;
import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
    private final List<ResultItem> items = new ArrayList<>();

    public void setItems(List<ResultItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResultItem item = items.get(position);
        holder.tvFilePath.setText(item.getFilePath());
        holder.tvStatus.setText(item.getStatus());
        
        int colorRes = item.isValid() ? R.color.success_green : R.color.error_red;
        holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorRes));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvFilePath;
        final TextView tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvFilePath = itemView.findViewById(R.id.tvResultFilePath);
            tvStatus = itemView.findViewById(R.id.tvResultStatus);
        }
    }
}

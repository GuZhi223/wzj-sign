package com.wzj.sign;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.wzj.sign.databinding.LogItemBinding;
import com.wzj.sign.log.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final List<LogEntry> entries = new ArrayList<>();

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        final LogItemBinding binding;

        LogViewHolder(LogItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LogItemBinding binding = LogItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = entries.get(position);
        holder.binding.tvTimestamp.setText(entry.getFormattedTime());
        holder.binding.tvMessage.setText(entry.getMessage());

        int colorRes;
        switch (entry.getLevel()) {
            case WARNING:
                colorRes = R.color.status_warning;
                break;
            case ERROR:
                colorRes = R.color.status_error;
                break;
            default:
                colorRes = R.color.status_success;
                break;
        }

        GradientDrawable dot = (GradientDrawable) holder.binding.viewLevelIndicator.getBackground();
        dot.setColor(ContextCompat.getColor(holder.itemView.getContext(), colorRes));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void addEntry(LogEntry entry) {
        entries.add(entry);
        notifyItemInserted(entries.size() - 1);
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
        notifyDataSetChanged();
    }

    public void clear() {
        int size = entries.size();
        entries.clear();
        notifyItemRangeRemoved(0, size);
    }

    public List<LogEntry> getEntries() {
        return new ArrayList<>(entries);
    }
}

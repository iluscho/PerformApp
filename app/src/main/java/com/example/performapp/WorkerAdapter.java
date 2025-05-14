package com.example.performapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.performapp.User;

import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {
    private final List<User> workerList;

    public WorkerAdapter(List<User> workerList) {
        this.workerList = workerList;
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new WorkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        holder.bind(workerList.get(position));
    }

    @Override
    public int getItemCount() {
        return workerList.size();
    }

    static class WorkerViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }

        public void bind(User user) {
            textView.setText(user.getLogin());
        }
    }
}


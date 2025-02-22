package com.example.performapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private TaskItemListener listener;

    public interface TaskItemListener {
        void onAcceptClicked(Task task);
        void onBookClicked(Task task);
    }

    public TaskAdapter(List<Task> tasks, TaskItemListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAddress, tvComment;
        private Button btnAccept, btnBook;

        public TaskViewHolder(View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvItemAddress);
            tvComment = itemView.findViewById(R.id.tvItemComment);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnBook = itemView.findViewById(R.id.btnBook);
        }

        public void bind(final Task task) {
            tvAddress.setText(task.getAddress());
            tvComment.setText(task.getComment());

            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onAcceptClicked(task);
                    }
                }
            });

            btnBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onBookClicked(task);
                    }
                }
            });
        }
    }
}

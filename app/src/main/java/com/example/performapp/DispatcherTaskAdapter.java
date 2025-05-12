package com.example.performapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherTaskAdapter extends RecyclerView.Adapter<DispatcherTaskAdapter.DispatcherViewHolder> {
    private final List<Task> taskList;
    private final OnRemoveWorkerClickListener listener;
    public interface OnRemoveWorkerClickListener {
        void onRemoveWorkerClicked(Task task);
    }

    public DispatcherTaskAdapter(List<Task> taskList, OnRemoveWorkerClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull @Override
    public DispatcherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_dispatcher, parent, false);
        return new DispatcherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DispatcherViewHolder holder, int position) {
        holder.bind(taskList.get(position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class DispatcherViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAddress, tvOrganization, tvWorker, tvStatus,
                tvTaskDate, tvAcceptanceDate, tvCompletionDate, tvComment;
        private final Button btnRemoveWorker, btnCancelTask;
        private final LinearLayout detailsContainer;


        public DispatcherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress        = itemView.findViewById(R.id.tvAddress);
            tvOrganization   = itemView.findViewById(R.id.tvOrganization);
            tvWorker         = itemView.findViewById(R.id.tvWorker);
            tvStatus         = itemView.findViewById(R.id.tvStatus);
            tvTaskDate       = itemView.findViewById(R.id.tvTaskDate);
            tvAcceptanceDate = itemView.findViewById(R.id.tvAcceptanceDate);
            tvCompletionDate = itemView.findViewById(R.id.tvCompletionDate);
            tvComment        = itemView.findViewById(R.id.tvComment);
            btnRemoveWorker  = itemView.findViewById(R.id.btnRemoveWorker);
            btnCancelTask = itemView.findViewById(R.id.btnCancelTask);
            detailsContainer = itemView.findViewById(R.id.detailsContainer);

            itemView.setOnClickListener(v -> {
                int vis = detailsContainer.getVisibility() == View.GONE
                        ? View.VISIBLE
                        : View.GONE;
                detailsContainer.setVisibility(vis);
            });
        }

        public void bind(Task task) {
            tvAddress.setText(task.getAddress());
            tvOrganization.setText("Объект: " + task.getOrganization());

            String wn = task.getWorkerName();
            tvWorker.setText("Работник: " + (wn.isEmpty() ? "не назначен" : wn));

            TaskStatus status = task.getStatus();
            tvStatus.setText("Статус: " + status.name());

            tvTaskDate.setText("Дата заявки: " + task.getTaskDate());
            tvAcceptanceDate.setText("Принято: " + task.getAcceptanceDate());
            tvCompletionDate.setText("Завершено: " + task.getCompletionDate());
            tvComment.setText("Комментарий: " + task.getComment());

            boolean canRemove = (status == TaskStatus.ACCEPTED || status == TaskStatus.BOOKED)
                    && !wn.isEmpty();
            btnRemoveWorker.setEnabled(canRemove);
            btnRemoveWorker.setVisibility(canRemove ? View.VISIBLE : View.GONE);

            btnRemoveWorker.setOnClickListener(v -> {
                if (!canRemove) return;
                updateWorkerAndStatus(task, v);
            });
            if (task.getStatus() != TaskStatus.CANCELLED && task.getStatus() != TaskStatus.COMPLETED) {
                btnCancelTask.setVisibility(View.VISIBLE);
                btnCancelTask.setOnClickListener(v -> cancelTask(task, v));
            } else {
                btnCancelTask.setVisibility(View.GONE);
            }

        }
        private void cancelTask(Task task, View anchor) {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("tasks")
                    .child(task.getId());

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", TaskStatus.CANCELLED.name());

            ref.updateChildren(updates).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(anchor.getContext(), "Задача снята", Toast.LENGTH_SHORT).show();
                    task.setStatus(TaskStatus.CANCELLED);
                    notifyItemChanged(getAdapterPosition());
                } else {
                    Toast.makeText(anchor.getContext(), "Ошибка при снятии задачи", Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void updateWorkerAndStatus(Task task, View anchor) {
            DatabaseReference ref = FirebaseDatabase
                    .getInstance()
                    .getReference("tasks")
                    .child(task.getId());

            Map<String, Object> upd = new HashMap<>();
            upd.put("workerName", "");
            upd.put("acceptanceDate", "");
            upd.put("status", TaskStatus.PENDING.name());

            ref.updateChildren(upd).addOnCompleteListener(op -> {
                if (op.isSuccessful()) {
                    Log.d("Firebase", "Обновили workerName, acceptanceDate и status у " + task.getId());
                    task.setWorkerName("");
                    task.setAcceptanceDate("");
                    task.setStatus(TaskStatus.PENDING);
                    listener.onRemoveWorkerClicked(task);
                    notifyItemChanged(getAdapterPosition());
                } else {
                    Log.e("Firebase", "Ошибка обновления", op.getException());
                    Toast.makeText(
                            anchor.getContext(),
                            "Не удалось обновить задачу: " + op.getException().getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }
}

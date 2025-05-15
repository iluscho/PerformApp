package com.example.performapp;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private TaskItemListener listener;
    private String currentWorkerLogin;

    public interface TaskItemListener {
        void onAcceptClicked(Task task);
        void onBookClicked(Task task);
    }

    public TaskAdapter(List<Task> tasks, TaskItemListener listener, String currentWorkerLogin) {
        this.tasks = tasks;
        this.listener = listener;
        this.currentWorkerLogin = currentWorkerLogin;
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
        private TextView tvAddress, tvComment, tvTimer, tvBookedBy;
        private Button btnAccept, btnBook;

        private Handler handler = new Handler();
        private Runnable updateTimerRunnable;

        public TaskViewHolder(View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvItemAddress);
            tvComment = itemView.findViewById(R.id.tvItemComment);
            tvTimer = itemView.findViewById(R.id.tvTimer);
            tvBookedBy = itemView.findViewById(R.id.tvBookedBy); // новый элемент
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnBook = itemView.findViewById(R.id.btnBook);
        }

        public void bind(final Task task) {
            tvAddress.setText(task.getAddress());
            tvComment.setText(task.getComment());

            // ✅ Отображение текста "Забронировано: ..."
            if (task.getStatus() == TaskStatus.BOOKED && task.getWorkerName() != null && !task.getWorkerName().isEmpty()) {
                tvBookedBy.setVisibility(View.VISIBLE);
                tvBookedBy.setText("Забронировано: " + task.getWorkerName());

                if (task.getWorkerName().equals(currentWorkerLogin)) {
                    // красный цвет — если забронировал текущий пользователь
                    tvBookedBy.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    // серый цвет — если забронировал кто-то другой
                    tvBookedBy.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                }
            } else {
                tvBookedBy.setVisibility(View.GONE);
            }

            // ⏱ Таймер
            final long startTime = parseDateToMillis(task.getTaskDate());

            if (updateTimerRunnable != null) {
                handler.removeCallbacks(updateTimerRunnable);
            }

            updateTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    long elapsedMillis = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsedMillis / 1000) % 60;
                    int minutes = (int) ((elapsedMillis / (1000 * 60)) % 60);
                    int hours = (int) ((elapsedMillis / (1000 * 60 * 60)));

                    String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                    tvTimer.setText(time);

                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(updateTimerRunnable);

            // Кнопки
            btnAccept.setOnClickListener(v -> listener.onAcceptClicked(task));
            btnBook.setOnClickListener(v -> listener.onBookClicked(task));
        }


        public void stopTimer() {
            handler.removeCallbacks(updateTimerRunnable);
        }

        private long parseDateToMillis(String taskDate) {
            if (taskDate == null || taskDate.isEmpty()) {
                return System.currentTimeMillis();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date date = sdf.parse(taskDate);
                return date != null ? date.getTime() : System.currentTimeMillis();
            } catch (ParseException e) {
                e.printStackTrace();
                return System.currentTimeMillis();
            }
        }
    }
}

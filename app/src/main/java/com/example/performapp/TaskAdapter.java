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
        private TextView tvAddress, tvComment, tvTimer;
        private Button btnAccept, btnBook;

        private Handler handler = new Handler();
        private Runnable updateTimerRunnable;

        public TaskViewHolder(View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvItemAddress);
            tvComment = itemView.findViewById(R.id.tvItemComment);
            tvTimer = itemView.findViewById(R.id.tvTimer);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnBook = itemView.findViewById(R.id.btnBook);
        }

        public void bind(final Task task) {
            tvAddress.setText(task.getAddress());
            tvComment.setText(task.getComment());

            // Преобразуем taskDate в миллисекунды
            final long startTime = parseDateToMillis(task.getTaskDate());

            // Запускаем таймер сразу после того, как привязаны данные
            if (updateTimerRunnable == null) {
                updateTimerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        long currentTimeMillis = System.currentTimeMillis(); // текущее время с учетом часового пояса
                        long elapsedMillis = currentTimeMillis - startTime;  // разница в миллисекундах

                        if (elapsedMillis < 0) {
                            // Если elapsedMillis меньше нуля, значит время taskDate в будущем, показываем 00:00:00
                            tvTimer.setText("00:00:00");
                        } else {
                            int seconds = (int) (elapsedMillis / 1000) % 60;
                            int minutes = (int) (elapsedMillis / (1000 * 60)) % 60;
                            int hours = (int) (elapsedMillis / (1000 * 60 * 60)) % 24;

                            String timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                            tvTimer.setText(timerText);
                        }

                        // Повторное обновление через 1 секунду
                        handler.postDelayed(this, 1000);
                    }
                };
            }

            handler.post(updateTimerRunnable);

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


        // Остановить таймер, если задача удалена или завершена
        public void stopTimer() {
            handler.removeCallbacks(updateTimerRunnable);
        }

        // Метод для преобразования строки времени в миллисекунды с учетом часового пояса
        // Метод для преобразования строки времени в миллисекунды с учетом часового пояса
        private long parseDateToMillis(String taskDate) {
            if (taskDate == null || taskDate.isEmpty()) {
                return System.currentTimeMillis(); // Если taskDate пустое, используем текущее время
            }

            // Используем правильный формат, соответствующий формату даты в базе данных
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date date = sdf.parse(taskDate);  // Преобразуем строку в объект Date
                return date != null ? date.getTime() : System.currentTimeMillis();  // Если парсинг успешен, возвращаем время, иначе текущее
            } catch (ParseException e) {
                e.printStackTrace();
                return System.currentTimeMillis();  // Если ошибка, используем текущее время
            }
        }

    }
}

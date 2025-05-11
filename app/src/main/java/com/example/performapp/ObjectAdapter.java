package com.example.performapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.ObjectViewHolder> {

    private final List<RegistryObject> objectList;

    public ObjectAdapter(List<RegistryObject> objectList) {
        this.objectList = objectList;
    }

    @NonNull
    @Override
    public ObjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_object, parent, false);
        return new ObjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ObjectViewHolder holder, int position) {
        RegistryObject obj = objectList.get(position);
        holder.nameTextView.setText(obj.getName());
        holder.addressTextView.setText(obj.getAddress());
        holder.descriptionTextView.setText(obj.getDescription());

        holder.deleteButton.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("objects")
                    .child(obj.getId())
                    .removeValue()
                    .addOnSuccessListener(unused ->
                            Toast.makeText(holder.itemView.getContext(), "Объект удалён", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(holder.itemView.getContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    public static class ObjectViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, addressTextView, descriptionTextView;
        Button deleteButton;

        public ObjectViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvName);
            addressTextView = itemView.findViewById(R.id.tvAddress);
            descriptionTextView = itemView.findViewById(R.id.tvDescription);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }
}

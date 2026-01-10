package com.orionverse.devplatform.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.models.Answer;
import com.orionverse.devplatform.utils.FirebaseUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder> {
    
    private List<Answer> answers = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    private OnAnswerActionListener actionListener;
    
    public interface OnAnswerActionListener {
        void onEditClick(Answer answer, int position);
        void onDeleteClick(Answer answer, int position);
    }
    
    public void setOnAnswerActionListener(OnAnswerActionListener listener) {
        this.actionListener = listener;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
        notifyDataSetChanged();
    }

    public void addAnswer(Answer answer) {
        answers.add(0, answer); // Add at top
        notifyItemInserted(0);
    }
    
    public void removeAnswer(int position) {
        if (position >= 0 && position < answers.size()) {
            answers.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void updateAnswer(int position, Answer answer) {
        if (position >= 0 && position < answers.size()) {
            answers.set(position, answer);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_answer, parent, false);
        return new AnswerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        Answer answer = answers.get(position);
        holder.bind(answer, position);
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }

    class AnswerViewHolder extends RecyclerView.ViewHolder {
        private TextView authorName;
        private TextView answerTime;
        private TextView answerContent;
        private LinearLayout actionButtons;
        private ImageButton editButton;
        private ImageButton deleteButton;

        public AnswerViewHolder(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.answerAuthorName);
            answerTime = itemView.findViewById(R.id.answerTime);
            answerContent = itemView.findViewById(R.id.answerContent);
            actionButtons = itemView.findViewById(R.id.actionButtons);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Answer answer, int position) {
            if (authorName != null) authorName.setText(answer.getAuthorName() != null ? answer.getAuthorName() : "Unknown");
            if (answerContent != null) answerContent.setText(answer.getContent() != null ? answer.getContent() : "");
            
            if (answerTime != null) {
                Timestamp timestamp = answer.getCreatedAt();
                if (timestamp != null) {
                    Date date = timestamp.toDate();
                    answerTime.setText(dateFormat.format(date));
                } else {
                    answerTime.setText("Just now");
                }
            }
            
            // Show action buttons only for the answer author
            String currentUserId = FirebaseUtil.getCurrentUserId();
            boolean isAuthor = currentUserId != null && currentUserId.equals(answer.getAuthorId());
            
            if (actionButtons != null) {
                actionButtons.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            }
            
            if (editButton != null && isAuthor) {
                editButton.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onEditClick(answer, position);
                    }
                });
            }
            
            if (deleteButton != null && isAuthor) {
                deleteButton.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onDeleteClick(answer, position);
                    }
                });
            }
        }
    }
}
    
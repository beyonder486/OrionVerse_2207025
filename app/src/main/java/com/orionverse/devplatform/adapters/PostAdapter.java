package com.orionverse.devplatform.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.orionverse.devplatform.R;
import com.orionverse.devplatform.activities.PostDetailActivity;
import com.orionverse.devplatform.models.Post;
import com.orionverse.devplatform.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private static final String TAG = "PostAdapter";
    private Context context;
    private List<Post> posts;
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(String postId);
    }

    public PostAdapter(Context context) {
        this.context = context;
        this.posts = new ArrayList<>();
    }

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        Log.d(TAG, "setPosts: Setting " + posts.size() + " posts");
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: CREATING VIEW HOLDER");
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        Log.d(TAG, "onBindViewHolder: BINDING position " + position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postTypeBadge, postTitle, postDescription, authorName, postTime;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postTypeBadge = itemView.findViewById(R.id.postTypeBadge);
            postTitle = itemView.findViewById(R.id.postTitle);
            postDescription = itemView.findViewById(R.id.postDescription);
            authorName = itemView.findViewById(R.id.authorName);
            postTime = itemView.findViewById(R.id.postTime);
        }

        public void bind(Post post) {
            postTitle.setText(post.getTitle());
            postDescription.setText(post.getDescription());
            authorName.setText(post.getAuthorName());
            postTime.setText(DateUtil.getRelativeTime(post.getCreatedAt()));

            // Set post type badge
            String type = post.getPostType();
            postTypeBadge.setText(type);

            // Set badge color based on type
            GradientDrawable background = (GradientDrawable) postTypeBadge.getBackground();
            if (type.equals("PROBLEM")) {
                background.setColor(context.getResources().getColor(R.color.problem_color));
            } else if (type.equals("SOLUTION")) {
                background.setColor(context.getResources().getColor(R.color.solution_color));
            } else {
                background.setColor(context.getResources().getColor(R.color.general_color));
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post.getPostId());
                }
            });
        }
    }
}

package com.orionverse.devplatform.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.orionverse.devplatform.R;
import com.orionverse.devplatform.models.Application;
import com.orionverse.devplatform.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private Context context;
    private List<Application> applications;
    private OnApplicationActionListener listener;

    public interface OnApplicationActionListener {
        void onAccept(Application application);
        void onReject(Application application);
        void onViewDetails(Application application);
    }

    public ApplicationAdapter(Context context) {
        this.context = context;
        this.applications = new ArrayList<>();
    }

    public void setOnApplicationActionListener(OnApplicationActionListener listener) {
        this.listener = listener;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_application, parent, false);
        return new ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        Application application = applications.get(position);
        holder.bind(application);
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    class ApplicationViewHolder extends RecyclerView.ViewHolder {
        TextView developerName, appliedTime, proposal, statusBadge;
        MaterialButton acceptButton, rejectButton;
        View actionButtons;

        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            developerName = itemView.findViewById(R.id.developerName);
            appliedTime = itemView.findViewById(R.id.appliedTime);
            proposal = itemView.findViewById(R.id.proposal);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            actionButtons = itemView.findViewById(R.id.actionButtons);
        }

        public void bind(Application application) {
            developerName.setText(application.getDeveloperName());
            appliedTime.setText(DateUtil.getRelativeTime(application.getAppliedAt()));
            proposal.setText(application.getProposal());

            // Set status badge
            String status = application.getStatus();
            statusBadge.setText(status);

            if (status.equals("PENDING")) {
                statusBadge.setBackgroundColor(context.getResources().getColor(R.color.warning_color));
                actionButtons.setVisibility(View.VISIBLE);
            } else if (status.equals("ACCEPTED")) {
                statusBadge.setBackgroundColor(context.getResources().getColor(R.color.success_color));
                actionButtons.setVisibility(View.GONE);
            } else if (status.equals("REJECTED")) {
                statusBadge.setBackgroundColor(context.getResources().getColor(R.color.error_color));
                actionButtons.setVisibility(View.GONE);
            }

            // Set click listeners
            acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccept(application);
                }
            });

            rejectButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReject(application);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(application);
                }
            });
        }
    }
}

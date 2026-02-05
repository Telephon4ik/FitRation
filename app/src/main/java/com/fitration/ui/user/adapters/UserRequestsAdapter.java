package com.fitration.ui.user.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitration.R;
import com.fitration.models.CoachRequest;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class UserRequestsAdapter extends RecyclerView.Adapter<UserRequestsAdapter.RequestViewHolder> {

    private List<CoachRequest> requests;
    private boolean showIncoming;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onRequestAction(CoachRequest request, boolean isIncoming);
    }

    public UserRequestsAdapter(boolean showIncoming) {
        this.showIncoming = showIncoming;
    }

    public void setRequests(List<CoachRequest> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }

    public void setOnRequestActionListener(OnRequestActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_request, parent, false);
        return new RequestViewHolder(view, showIncoming);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        CoachRequest request = requests.get(position);
        holder.bind(request);

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRequestAction(request, showIncoming);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests != null ? requests.size() : 0;
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRequestInfo;
        private final TextView tvRequestDate;
        private final TextView tvRequestStatus;
        private final TextView tvMessage;
        private final Button btnAction;
        private final boolean showIncoming;

        public RequestViewHolder(@NonNull View itemView, boolean showIncoming) {
            super(itemView);
            this.showIncoming = showIncoming;

            tvRequestInfo = itemView.findViewById(R.id.tv_coach_name);
            tvRequestDate = itemView.findViewById(R.id.tv_request_date);
            tvRequestStatus = itemView.findViewById(R.id.tv_status);
            tvMessage = itemView.findViewById(R.id.tv_message);
            btnAction = itemView.findViewById(R.id.btn_action);
        }

        public void bind(CoachRequest request) {
            if (showIncoming) {
                // Входящая заявка от тренера
                tvRequestInfo.setText("Тренер: " + request.getCoachName() +
                        "\nID: " + request.getCoachId());
            } else {
                // Отправленная заявка пользователя
                tvRequestInfo.setText("Тренер: " + request.getCoachName() +
                        "\nID: " + request.getCoachId());
            }

            if (request.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                tvRequestDate.setText(sdf.format(request.getCreatedAt()));
            }

            String status = request.getStatus();
            tvRequestStatus.setText(getStatusText(status));
            tvMessage.setText(request.getMessage() != null ?
                    request.getMessage() : "Без сообщения");

            // Настройка кнопки действия
            if ("PENDING".equals(status)) {
                if (showIncoming) {
                    btnAction.setText("Ответить");
                    btnAction.setVisibility(View.VISIBLE);
                } else {
                    btnAction.setText("Отменить");
                    btnAction.setVisibility(View.VISIBLE);
                }
            } else {
                btnAction.setVisibility(View.GONE);
            }
        }

        private String getStatusText(String status) {
            if (status == null) {
                return "Неизвестно";
            }
            switch (status) {
                case "PENDING": return "Ожидает";
                case "ACCEPTED": return "Принята";
                case "REJECTED": return "Отклонена";
                case "CANCELLED": return "Отменена";
                default: return status;
            }
        }
    }
}
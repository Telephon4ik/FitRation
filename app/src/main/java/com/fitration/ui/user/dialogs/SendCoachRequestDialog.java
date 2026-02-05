package com.fitration.ui.user.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.fitration.R;
import com.fitration.viewmodels.UserViewModel;

public class SendCoachRequestDialog extends DialogFragment {

    private EditText etCoachId;
    private EditText etMessage;
    private Button btnSend;
    private Button btnCancel;
    private TextView tvCoachInfo;

    private String coachName;
    private String coachPublicId;
    private OnRequestSentListener listener;
    private UserViewModel userViewModel;

    public interface OnRequestSentListener {
        void onRequestSent(String coachId, String message);
    }

    public SendCoachRequestDialog(String coachName, String coachPublicId) {
        this.coachName = coachName;
        this.coachPublicId = coachPublicId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_send_coach_request, null);

        initViews(view);
        setupViewModels();
        setupListeners();

        builder.setView(view)
                .setTitle("Отправить заявку тренеру");

        return builder.create();
    }

    private void initViews(View view) {
        etCoachId = view.findViewById(R.id.et_request_coach_id);
        etMessage = view.findViewById(R.id.et_request_message);
        btnSend = view.findViewById(R.id.btn_send_request);
        btnCancel = view.findViewById(R.id.btn_cancel_request);
        tvCoachInfo = view.findViewById(R.id.tv_coach_info);

        // Заполняем информацию о тренере
        if (coachName != null && coachPublicId != null) {
            etCoachId.setText(coachPublicId);
            etCoachId.setEnabled(false);
            tvCoachInfo.setText("Тренер: " + coachName + " (" + coachPublicId + ")");
        }
    }

    private void setupViewModels() {
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendRequest());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void sendRequest() {
        String coachId = etCoachId.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (coachId.isEmpty()) {
            etCoachId.setError("Введите ID тренера");
            return;
        }

        if (!coachId.startsWith("FR-")) {
            etCoachId.setError("ID тренера должен начинаться с FR-");
            return;
        }

        if (listener != null) {
            listener.onRequestSent(coachId, message);
        }

        dismiss();
    }

    public void setOnRequestSentListener(OnRequestSentListener listener) {
        this.listener = listener;
    }
}
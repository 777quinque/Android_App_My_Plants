package app.rifa.plantcareapp.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import app.rifa.plantcareapp.core.forgot.ForgotContract;
import app.rifa.plantcareapp.core.forgot.ForgotPresenter;
import app.rifa.plantcareapp.databinding.DialogForgotPasswordBinding;
import app.rifa.plantcareapp.utils.ProgressDialogUtils;

public class ForgotDialog extends Dialog implements ForgotContract.View {
    ProgressDialog progressDialog;
    private ForgotPresenter presenter;
    private DialogForgotPasswordBinding binding;

    public ForgotDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DialogForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        presenter = new ForgotPresenter(this);

        binding.btResetPassword.setOnClickListener(v ->
                presenter.resetPassword(binding.etEmail.getText().toString()));
    }

    @Override
    public void onSendEmailSuccess(int message) {
        showMessage(message);
        dismiss();
    }

    @Override
    public void setEmailError(int error) {
        binding.etEmail.setError(getContext().getString(error));
    }

    @Override
    public void onSendEmailFailure(String message) {
        showMessage(message);
    }

    @Override
    public void showLoading() {
        hideLoading();
        progressDialog = ProgressDialogUtils.showLoadingDialog(getContext());
    }

    @Override
    public void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(int resId) {
        showMessage(getContext().getString(resId));
    }

}

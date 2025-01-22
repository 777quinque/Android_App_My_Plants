package app.rifa.plantcareapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import app.rifa.plantcareapp.R;

public class ProgressDialogUtils {

    public static ProgressDialog showLoadingDialog(Context context) {
        if (context != null && !((Activity) context).isFinishing()) {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.show();
            if (progressDialog.getWindow() != null) {
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            progressDialog.setContentView(R.layout.dialog_progress);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            return progressDialog;
        }
        return null;
    }
}

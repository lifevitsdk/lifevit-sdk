package es.lifevit.sdk.sampleapp.activities.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import es.lifevit.sdk.sampleapp.R;

public abstract class BaseAppCompatActivity extends AppCompatActivity {

    private static final String TAG = BaseAppCompatActivity.class.getSimpleName();


    public interface RequestAcceptListener {
        void onRequestAccepted(boolean accepted);
    }

    private RequestAcceptListener requestAcceptListener;
    protected boolean isLocked;


    public void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.dialog_error_title));
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (!this.isFinishing()) {
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        // Mostrar mensaje
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (!this.isFinishing()) {
            builder.show();
        }
    }


    public void showConfirmMessage(String message, String buttonOk, boolean cancelButton, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage(message);
        builder.setPositiveButton(buttonOk, okListener);

        if (cancelButton) {
            builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
        }

        //2. now setup to change color of the button
        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                int color = ContextCompat.getColor(BaseAppCompatActivity.this, R.color.colorPrimary);
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(color);
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(color);
            }
        });
        dialog.show();
    }


    @SuppressLint("NewApi")
    public void requestPermission(View main, String permission, int requestCode, int errorMessage, boolean indefinite, RequestAcceptListener listener) {
        requestAcceptListener = listener;

        if (Build.VERSION.SDK_INT < 23 || ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            requestAcceptListener.onRequestAccepted(true);
        } else {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(permission)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showPermissionDenied(main, getResources().getString(errorMessage), indefinite);

                requestAcceptListener.onRequestAccepted(false);
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{permission}, requestCode);
            }
        }
    }


    public void requestPermission(View main, String permission, int requestCode, int errorMessage, RequestAcceptListener listener) {
        requestPermission(main, permission, requestCode, errorMessage, false, listener);
    }


    /**
     * Request for multiple permissions
     */
    @SuppressLint("NewApi")
    public void requestPermissions(View main, String[] permissions, int requestCode, int message, boolean indefinite, RequestAcceptListener listener) {

        requestAcceptListener = listener;

        boolean allGranted = true;
        for (String permission : permissions) {
            allGranted = allGranted && ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        }

        if (Build.VERSION.SDK_INT < 23 || allGranted) {
            requestAcceptListener.onRequestAccepted(true);
        } else {
            boolean shouldRequestPermission = true;
            for (String permission : permissions) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(permission)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    showPermissionDenied(main, getResources().getString(message), indefinite);
                    requestAcceptListener.onRequestAccepted(false);
                    shouldRequestPermission = false;
                    break;
                }
            }
            if (shouldRequestPermission) {
                // No explanation needed, we can request the permission.
                requestPermissions(permissions, requestCode);
            }
        }
    }


    // esta classe mostra la snack bar amb un missatge, fer que cada cop que faci falta es cridi d'aquí
    public void showPermissionDenied(View main, String message, boolean indefinite) {
        try {
            Snackbar snackbar = Snackbar.make(main, message, indefinite ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG);
            snackbar.setAction(getResources().getString(R.string.settings), v -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });
            snackbar.show();
        } catch (Exception e) {
            //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            if (requestAcceptListener != null) {
                requestAcceptListener.onRequestAccepted(true);
            }
        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            if (requestAcceptListener != null) {
                requestAcceptListener.onRequestAccepted(false);
            }
        }
    }


    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show, final View mProgressView) {
        if (isLocked != show) {
            isLocked = show;
            if (mProgressView != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    mProgressView.animate().setDuration(shortAnimTime).alpha(
                            show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                            mProgressView.requestFocus();
                        }
                    });
                } else {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    mProgressView.requestFocus();
                }
            }
        }
    }

}

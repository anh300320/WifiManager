package Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.wifimanager.R;
import com.example.wifimanager.SaveFileManager;

import Objects.OnSavedDevice;

public class SaveDeviceDialog extends DialogFragment {

    EditText etName;
    String name = "";
    OnSavedDevice onSavedDevice;

    public void setOnSavedDevice(OnSavedDevice onSavedDevice){
        this.onSavedDevice = onSavedDevice;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.dialog_save_device, null);
        builder.setView(customLayout);
        builder.setTitle("Đặt tên cho thiết bị này");

        etName = customLayout.findViewById(R.id.dialog_save_device_name);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name = etName.getText().toString();
                onSavedDevice.onSaved(name);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }
}

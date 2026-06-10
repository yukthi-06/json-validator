package com.example.jsonvalidator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.jsonvalidator.R;
import com.example.jsonvalidator.model.AppSettings;
import com.google.android.material.button.MaterialButton;

public class SettingsFragment extends Fragment {

    private EditText editPath;
    private AppSettings settings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editPath = view.findViewById(R.id.editPath);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveSettings);

        settings = AppSettings.load();
        editPath.setText(settings.defaultValidationPath);

        btnSave.setOnClickListener(v -> {
            String newPath = editPath.getText().toString().trim();
            if (!newPath.isEmpty()) {
                if (!newPath.endsWith("/")) {
                    newPath += "/";
                }
                settings.defaultValidationPath = newPath;
                settings.save();
                Toast.makeText(requireContext(), "Settings saved to settings.json", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Path cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

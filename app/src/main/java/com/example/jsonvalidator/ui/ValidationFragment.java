package com.example.jsonvalidator.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jsonvalidator.R;
import com.example.jsonvalidator.adapter.ResultAdapter;
import com.example.jsonvalidator.viewmodel.ValidationViewModel;
import com.google.android.material.button.MaterialButton;

public class ValidationFragment extends Fragment {

    private ValidationViewModel viewModel;
    private ResultAdapter adapter;

    private TextView tvSelectedPath;
    private MaterialButton btnSelectFile;
    private MaterialButton btnSelectFolder;
    private MaterialButton btnStartValidation;
    private ProgressBar progressBar;
    
    private TextView tvTotalFiles;
    private TextView tvValidFiles;
    private TextView tvInvalidFiles;

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception ignored) {
                    }
                    String displayName = getFileName(uri);
                    viewModel.setSelectedFile(uri, displayName);
                }
            }
    );

    private final ActivityResultLauncher<Uri> folderPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri != null) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception ignored) {
                    }
                    viewModel.setSelectedFolder(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_validation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI Elements
        tvSelectedPath = view.findViewById(R.id.tvSelectedPath);
        btnSelectFile = view.findViewById(R.id.btnSelectFile);
        btnSelectFolder = view.findViewById(R.id.btnSelectFolder);
        btnStartValidation = view.findViewById(R.id.btnStartValidation);
        progressBar = view.findViewById(R.id.progressBar);
        
        tvTotalFiles = view.findViewById(R.id.tvTotalFiles);
        tvValidFiles = view.findViewById(R.id.tvValidFiles);
        tvInvalidFiles = view.findViewById(R.id.tvInvalidFiles);

        RecyclerView rvResults = view.findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ResultAdapter();
        rvResults.setAdapter(adapter);

        // Initialize ViewModel (Shared with Activity if necessary, or fragment level)
        viewModel = new ViewModelProvider(requireActivity()).get(ValidationViewModel.class);

        // Set Click Listeners
        btnSelectFile.setOnClickListener(v -> filePickerLauncher.launch(new String[]{"application/json", "text/*", "*/*"}));
        btnSelectFolder.setOnClickListener(v -> folderPickerLauncher.launch(null));
        btnStartValidation.setOnClickListener(v -> viewModel.startValidation());

        // Observe ViewModel LiveData
        viewModel.getSelectedPathString().observe(getViewLifecycleOwner(), path -> {
            if (path == null || path.isEmpty()) {
                tvSelectedPath.setText(R.string.text_no_selection);
                btnStartValidation.setEnabled(false);
            } else {
                tvSelectedPath.setText(path);
                btnStartValidation.setEnabled(true);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                btnSelectFile.setEnabled(false);
                btnSelectFolder.setEnabled(false);
                btnStartValidation.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                btnSelectFile.setEnabled(true);
                btnSelectFolder.setEnabled(true);
                btnStartValidation.setEnabled(viewModel.getSelectedUri() != null);
            }
        });

        viewModel.getResults().observe(getViewLifecycleOwner(), results -> {
            adapter.setItems(results);
        });

        viewModel.getTotalFilesCount().observe(getViewLifecycleOwner(), count -> {
            tvTotalFiles.setText(String.valueOf(count));
        });

        viewModel.getValidFilesCount().observe(getViewLifecycleOwner(), count -> {
            tvValidFiles.setText(String.valueOf(count));
        });

        viewModel.getInvalidFilesCount().observe(getViewLifecycleOwner(), count -> {
            tvInvalidFiles.setText(String.valueOf(count));
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}

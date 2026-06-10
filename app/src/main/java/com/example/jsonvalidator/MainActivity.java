package com.example.jsonvalidator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jsonvalidator.adapter.ResultAdapter;
import com.example.jsonvalidator.viewmodel.ValidationViewModel;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

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
                    // Take persistable URI permission if needed
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception ignored) {
                        // Persistable URI might not be supported or required for single URI
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
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception ignored) {
                    }
                    viewModel.setSelectedFolder(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI Elements
        tvSelectedPath = findViewById(R.id.tvSelectedPath);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnStartValidation = findViewById(R.id.btnStartValidation);
        progressBar = findViewById(R.id.progressBar);
        
        tvTotalFiles = findViewById(R.id.tvTotalFiles);
        tvValidFiles = findViewById(R.id.tvValidFiles);
        tvInvalidFiles = findViewById(R.id.tvInvalidFiles);

        RecyclerView rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResultAdapter();
        rvResults.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ValidationViewModel.class);

        // Set Click Listeners
        btnSelectFile.setOnClickListener(v -> filePickerLauncher.launch(new String[]{"application/json", "text/*", "*/*"}));
        btnSelectFolder.setOnClickListener(v -> folderPickerLauncher.launch(null));
        btnStartValidation.setOnClickListener(v -> viewModel.startValidation());

        // Observe ViewModel LiveData
        viewModel.getSelectedPathString().observe(this, path -> {
            if (path == null || path.isEmpty()) {
                tvSelectedPath.setText(R.string.text_no_selection);
                btnStartValidation.setEnabled(false);
            } else {
                tvSelectedPath.setText(path);
                btnStartValidation.setEnabled(true);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
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

        viewModel.getResults().observe(this, results -> {
            adapter.setItems(results);
        });

        viewModel.getTotalFilesCount().observe(this, count -> {
            tvTotalFiles.setText(String.valueOf(count));
        });

        viewModel.getValidFilesCount().observe(this, count -> {
            tvValidFiles.setText(String.valueOf(count));
        });

        viewModel.getInvalidFilesCount().observe(this, count -> {
            tvInvalidFiles.setText(String.valueOf(count));
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
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

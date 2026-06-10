package com.example.jsonvalidator.viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.jsonvalidator.model.ResultItem;
import com.example.jsonvalidator.utils.JsonValidator;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ValidationViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ResultItem>> results = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> totalFilesCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> validFilesCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> invalidFilesCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> selectedPathString = new MutableLiveData<>("");

    private Uri selectedUri = null;
    private boolean isFolder = false;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ValidationViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<ResultItem>> getResults() {
        return results;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Integer> getTotalFilesCount() {
        return totalFilesCount;
    }

    public LiveData<Integer> getValidFilesCount() {
        return validFilesCount;
    }

    public LiveData<Integer> getInvalidFilesCount() {
        return invalidFilesCount;
    }

    public LiveData<String> getSelectedPathString() {
        return selectedPathString;
    }

    public void setSelectedFile(Uri uri, String displayName) {
        this.selectedUri = uri;
        this.isFolder = false;
        this.selectedPathString.setValue(displayName != null ? displayName : uri.getPath());
        resetStats();
    }

    public void setSelectedFolder(Uri uri) {
        this.selectedUri = uri;
        this.isFolder = true;
        
        String path = uri.getPath();
        if (path != null) {
            int index = path.indexOf("primary:");
            if (index != -1) {
                path = "storage/emulated/0/" + Uri.decode(path.substring(index + "primary:".length()));
            } else {
                path = Uri.decode(path);
            }
        } else {
            path = uri.toString();
        }
        
        this.selectedPathString.setValue(path);
        resetStats();
    }

    public Uri getSelectedUri() {
        return selectedUri;
    }

    private void resetStats() {
        results.setValue(new ArrayList<>());
        totalFilesCount.setValue(0);
        validFilesCount.setValue(0);
        invalidFilesCount.setValue(0);
    }

    public void startValidation() {
        if (selectedUri == null) {
            return;
        }

        isLoading.setValue(true);
        resetStats();

        executorService.execute(() -> {
            List<ResultItem> finalResults = new ArrayList<>();
            int total = 0;
            int valid = 0;
            int invalid = 0;

            try {
                if (isFolder) {
                    DocumentFile rootDir = DocumentFile.fromTreeUri(getApplication(), selectedUri);
                    if (rootDir != null && rootDir.exists()) {
                        List<DocumentFile> jsonFiles = new ArrayList<>();
                        scanDirectory(rootDir, jsonFiles);

                        total = jsonFiles.size();
                        postTotalCount(total);

                        for (DocumentFile file : jsonFiles) {
                            ResultItem item = validateFile(file);
                            if (item.isValid()) {
                                valid++;
                            } else {
                                invalid++;
                            }
                            finalResults.add(item);
                            postCurrentProgress(new ArrayList<>(finalResults), valid, invalid);
                        }
                    }
                } else {
                    DocumentFile file = DocumentFile.fromSingleUri(getApplication(), selectedUri);
                    if (file != null && file.exists()) {
                        total = 1;
                        postTotalCount(total);

                        ResultItem item = validateFile(file);
                        if (item.isValid()) {
                            valid++;
                        } else {
                            invalid++;
                        }
                        finalResults.add(item);
                        postCurrentProgress(finalResults, valid, invalid);
                    }
                }
            } catch (Exception e) {
                // If anything fails
                finalResults.add(new ResultItem(
                        selectedUri.toString(),
                        "ERROR - General exception: " + e.getMessage(),
                        false,
                        -1,
                        e.getMessage()
                ));
                invalid++;
                postCurrentProgress(finalResults, valid, invalid);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    private void scanDirectory(DocumentFile directory, List<DocumentFile> jsonFiles) {
        DocumentFile[] files = directory.listFiles();
        if (files != null) {
            for (DocumentFile file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, jsonFiles);
                } else {
                    String name = file.getName();
                    if (name != null && name.toLowerCase().endsWith(".json")) {
                        jsonFiles.add(file);
                    }
                }
            }
        }
    }

    private ResultItem validateFile(DocumentFile file) {
        String displayPath = getDisplayPath(file);
        try (InputStream inputStream = getApplication().getContentResolver().openInputStream(file.getUri())) {
            if (inputStream == null) {
                return new ResultItem(displayPath, "ERROR - Could not open file stream", false, -1, "Stream null");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            
            JsonValidator.ValidationResult validationResult = JsonValidator.validate(sb.toString());
            return new ResultItem(
                    displayPath,
                    validationResult.getStatusString(),
                    validationResult.isValid(),
                    validationResult.getLineNumber(),
                    validationResult.getErrorMessage()
            );
        } catch (Exception e) {
            return new ResultItem(
                    displayPath,
                    "ERROR - " + e.getMessage(),
                    false,
                    -1,
                    e.getMessage()
            );
        }
    }

    private String getDisplayPath(DocumentFile file) {
        String path = file.getUri().getPath();
        if (path != null) {
            int index = path.indexOf("primary:");
            if (index != -1) {
                return "storage/emulated/0/" + Uri.decode(path.substring(index + "primary:".length()));
            }
            int docIndex = path.indexOf("/document/");
            if (docIndex != -1) {
                return Uri.decode(path.substring(docIndex + "/document/".length()));
            }
            return Uri.decode(path);
        }
        return file.getName() != null ? file.getName() : "Unknown File";
    }

    private void postTotalCount(int total) {
        totalFilesCount.postValue(total);
    }

    private void postCurrentProgress(List<ResultItem> currentResults, int valid, int invalid) {
        results.postValue(currentResults);
        validFilesCount.postValue(valid);
        invalidFilesCount.postValue(invalid);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}

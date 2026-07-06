package ke.ac.kca.dms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a document in the KCA Document Management System.
 * Handles creation, versioning, and metadata storage.
 */
public class Document {
    private String documentId;
    private String title;
    private String fileType;
    private String uploadedBy;
    private String currentVersion;
    private String status;
    private LocalDateTime createdAt;
    private List<String> versionHistory;

    public Document(String documentId, String title,
                    String fileType, String uploadedBy) {
        this.documentId = documentId;
        this.title = title;
        this.fileType = fileType;
        this.uploadedBy = uploadedBy;
        this.currentVersion = "v1.0";
        this.status = "DRAFT";
        this.createdAt = LocalDateTime.now();
        this.versionHistory = new ArrayList<>();
        this.versionHistory.add("v1.0");
    }

    // Getters
    public String getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getFileType() { return fileType; }
    public String getUploadedBy() { return uploadedBy; }
    public String getCurrentVersion() { return currentVersion; }
    public String getStatus() { return status; }
    public List<String> getVersionHistory() { return versionHistory; }

    public void setStatus(String status) { this.status = status; }
}
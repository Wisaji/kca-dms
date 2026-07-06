package ke.ac.kca.dms.service;

import ke.ac.kca.dms.model.Document;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service handling document upload logic.
 * Sequence: User -> DocumentUploadService.uploadDocument()
 *           -> validateFileType() -> new Document() -> AuditService.logAction()
 */
public class DocumentUploadService {

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "pdf", "docx", "xlsx", "pptx", "txt", "png", "jpg"
    );

    private final AuditService auditService;

    public DocumentUploadService(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Uploads a document to the system.
     * Validates file type, creates document object, and logs the action.
     */
    public Document uploadDocument(String fileName, String uploadedBy) {
        // Step 1: Extract and validate file type
        String fileType = extractFileType(fileName);

        if (!validateFileType(fileType)) {
            throw new IllegalArgumentException(
                "Unsupported file type: " + fileType +
                ". Allowed types: " + ALLOWED_TYPES
            );
        }

        // Step 2: Create document with initial version v1.0
        String docId = UUID.randomUUID().toString().substring(0, 8);
        Document document = new Document(docId, fileName, fileType, uploadedBy);

        // Step 3: Log the upload action in audit trail
        auditService.logAction(uploadedBy, "UPLOAD", docId,
            "Document '" + fileName + "' uploaded as version v1.0");

        return document;
    }

    public boolean validateFileType(String fileType) {
        return ALLOWED_TYPES.contains(fileType.toLowerCase());
    }

    private String extractFileType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            throw new IllegalArgumentException("File has no extension");
        }
        return fileName.substring(dotIndex + 1);
    }
}
package ke.ac.kca.dms.service;

import ke.ac.kca.dms.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for the Document Upload use case.
 * Verifies sequence logic: User -> Service -> Validation -> Document -> Audit
 */
class DocumentUploadServiceTest {

    private AuditService auditService;
    private DocumentUploadService uploadService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService();
        uploadService = new DocumentUploadService(auditService);
    }

    @Test
    @DisplayName("TC-01: Lecturer uploads lecture_notes.pdf - document created, versioned, and logged")
    void testSuccessfulDocumentUpload() {
        // INPUT: Lecturer uploads lecture_notes.pdf
        String fileName = "lecture_notes.pdf";
        String uploadedBy = "lecturer001";

        // ACT: Execute the upload sequence
        Document result = uploadService.uploadDocument(fileName, uploadedBy);

        // EXPECTED OUTPUT: Document is created with version v1.0 and audit logged
        assertNotNull(result,
            "Document object should be created");
        assertEquals("lecture_notes.pdf", result.getTitle(),
            "Title should match uploaded file name");
        assertEquals("pdf", result.getFileType(),
            "File type should be correctly extracted");
        assertEquals("v1.0", result.getCurrentVersion(),
            "Initial version should be v1.0");
        assertEquals("lecturer001", result.getUploadedBy(),
            "Uploader ID should be recorded");
        assertEquals("DRAFT", result.getStatus(),
            "Initial status should be DRAFT");

        // Verify version history contains v1.0
        assertTrue(result.getVersionHistory().contains("v1.0"),
            "Version history should contain v1.0");

        // Verify audit entry was logged
        assertEquals(1, auditService.getAuditLog().size(),
            "Exactly one audit entry should be recorded");
        assertEquals("UPLOAD",
            auditService.getAuditLog().get(0).action(),
            "Audit action should be UPLOAD");

        // ACTUAL RESULT: Document uploaded successfully and log recorded - PASS
    }

    @Test
    @DisplayName("TC-02: Unsupported file type rejected with validation message")
    void testUnsupportedFileTypeRejected() {
        // INPUT: User attempts to upload an unsupported .exe file
        String fileName = "malicious_file.exe";
        String uploadedBy = "lecturer001";

        // ACT & ASSERT: System rejects file with validation message
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> uploadService.uploadDocument(fileName, uploadedBy),
            "Should throw exception for unsupported file type"
        );

        // EXPECTED OUTPUT: Validation message displayed
        assertTrue(exception.getMessage().contains("Unsupported file type"),
            "Error message should indicate unsupported type");
        assertTrue(exception.getMessage().contains("exe"),
            "Error message should name the rejected extension");

        // Verify no document was created (no audit entry)
        assertEquals(0, auditService.getAuditLog().size(),
            "No audit entry should exist for rejected upload");

        // ACTUAL RESULT: Validation message displayed - PASS
    }

    @Test
    @DisplayName("Validates all supported file formats (pdf, docx, xlsx, pptx, txt, png, jpg)")
    void testAllSupportedFileTypes() {
        assertAll("All KCA DMS supported file types pass validation",
            () -> assertTrue(uploadService.validateFileType("pdf")),
            () -> assertTrue(uploadService.validateFileType("docx")),
            () -> assertTrue(uploadService.validateFileType("xlsx")),
            () -> assertTrue(uploadService.validateFileType("pptx")),
            () -> assertTrue(uploadService.validateFileType("txt")),
            () -> assertTrue(uploadService.validateFileType("png")),
            () -> assertTrue(uploadService.validateFileType("jpg"))
        );
    }

    @Test
    @DisplayName("Rejects file with no extension")
    void testFileWithNoExtension() {
        assertThrows(IllegalArgumentException.class,
            () -> uploadService.uploadDocument("noextension", "user01"),
            "File without extension should be rejected");
    }
}
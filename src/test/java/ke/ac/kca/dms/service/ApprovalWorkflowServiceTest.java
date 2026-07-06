package ke.ac.kca.dms.service;

import ke.ac.kca.dms.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for the Approval Workflow use case.
 * Verifies sequence: Initiator -> Service -> Document -> Notification -> Audit
 */
class ApprovalWorkflowServiceTest {

    private AuditService auditService;
    private NotificationService notificationService;
    private ApprovalWorkflowService approvalService;
    private Document policyDocument;

    @BeforeEach
    void setUp() {
        auditService = new AuditService();
        notificationService = new NotificationService();
        approvalService = new ApprovalWorkflowService(
            notificationService, auditService
        );

        // Setup: Policy document uploaded by management staff
        policyDocument = new Document(
            "DOC-2025-001",
            "KCA_ICT_Security_Policy.pdf",
            "pdf",
            "management_staff_01"
        );
    }

    @Test
    @DisplayName("TC-03: Management initiates approval - status IN_REVIEW, approver notified")
    void testInitiateApprovalWorkflow() {
        // INPUT: Management staff initiates approval for policy document
        String initiatorId = "management_staff_01";
        String approverId = "hod_ict_department";

        // ACT: Initiate the approval workflow
        approvalService.initiateApproval(policyDocument, initiatorId, approverId);

        // EXPECTED OUTPUT: Status changes to IN_REVIEW, approver is notified

        // Verify document status changed to IN_REVIEW
        assertEquals("IN_REVIEW", policyDocument.getStatus(),
            "Document status should change to IN_REVIEW");

        // Verify approver received notification
        assertTrue(notificationService.wasNotified(approverId),
            "Approver should receive a notification");

        // Verify notification content is meaningful
        String message = notificationService.getSentNotifications()
            .get(0).message();
        assertTrue(message.contains("requires your approval"),
            "Notification should request approval action");
        assertTrue(message.contains(policyDocument.getTitle()),
            "Notification should reference the document");

        // Verify audit log entry
        assertEquals(1, auditService.getAuditLog().size(),
            "One audit entry should be created");
        assertEquals("INITIATE_APPROVAL",
            auditService.getAuditLog().get(0).action(),
            "Audit action should be INITIATE_APPROVAL");

        // ACTUAL RESULT: Status updated and notification sent - PASS
    }

    @Test
    @DisplayName("TC-04: Approver approves document - status APPROVED, audit recorded")
    void testApproveDocument() {
        // INPUT: Approver approves a document currently IN_REVIEW
        String approverId = "hod_ict_department";
        policyDocument.setStatus("IN_REVIEW"); // Pre-condition

        // ACT: Approver approves the document
        approvalService.approveDocument(policyDocument, approverId);

        // EXPECTED OUTPUT: Status changes to APPROVED, audit action recorded

        // Verify status changed to APPROVED
        assertEquals("APPROVED", policyDocument.getStatus(),
            "Document status should change to APPROVED");

        // Verify audit log records the approval
        assertEquals(1, auditService.getAuditLog().size(),
            "One audit entry should be created");
        assertEquals("APPROVE",
            auditService.getAuditLog().get(0).action(),
            "Audit action should be APPROVE");
        assertTrue(
            auditService.getAuditLog().get(0).details()
                .contains(approverId),
            "Audit should record who approved");

        // Verify document owner is notified of approval
        assertTrue(
            notificationService.wasNotified("management_staff_01"),
            "Document owner should be notified of approval");

        // ACTUAL RESULT: Status changed to APPROVED and audit log created - PASS
    }

    @Test
    @DisplayName("TC-05: Approver rejects document with reason - status REJECTED, initiator notified")
    void testRejectDocumentWithReason() {
        // INPUT: Approver rejects document with a specific reason
        String approverId = "hod_ict_department";
        String rejectionReason = "Section 4.2 requires revision per new KCA regulations";
        policyDocument.setStatus("IN_REVIEW"); // Pre-condition

        // ACT: Approver rejects the document
        approvalService.rejectDocument(policyDocument, approverId, rejectionReason);

        // EXPECTED OUTPUT: Status REJECTED, initiator notified with reason

        // Verify status changed to REJECTED
        assertEquals("REJECTED", policyDocument.getStatus(),
            "Document status should change to REJECTED");

        // Verify audit log records rejection with reason
        assertEquals(1, auditService.getAuditLog().size(),
            "One audit entry should be created");
        assertEquals("REJECT",
            auditService.getAuditLog().get(0).action(),
            "Audit action should be REJECT");
        assertTrue(
            auditService.getAuditLog().get(0).details()
                .contains(rejectionReason),
            "Audit should include the rejection reason");

        // Verify initiator (document owner) receives notification
        assertTrue(
            notificationService.wasNotified("management_staff_01"),
            "Initiator should receive rejection notification");

        // Verify notification contains the rejection reason
        String notifMessage = notificationService.getSentNotifications()
            .get(0).message();
        assertTrue(notifMessage.contains("rejected"),
            "Notification should mention rejection");
        assertTrue(notifMessage.contains(rejectionReason),
            "Notification should include the reason");

        // ACTUAL RESULT: Document rejected and notification sent - PASS
    }

    @Test
    @DisplayName("Guard: Cannot approve document not in IN_REVIEW status")
    void testCannotApproveDocumentNotInReview() {
        // Document is still in DRAFT (not yet submitted for review)
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> approvalService.approveDocument(policyDocument, "hod_ict"),
            "Should not approve document that is not IN_REVIEW"
        );

        assertTrue(exception.getMessage().contains("IN_REVIEW"),
            "Error should indicate required status");
        assertEquals("DRAFT", policyDocument.getStatus(),
            "Status should remain unchanged after failed approval");
    }

    @Test
    @DisplayName("Guard: Cannot reject document not in IN_REVIEW status")
    void testCannotRejectDocumentNotInReview() {
        // Document already APPROVED
        policyDocument.setStatus("APPROVED");

        assertThrows(IllegalStateException.class,
            () -> approvalService.rejectDocument(
                policyDocument, "hod_ict", "Too late"),
            "Should not reject an already approved document"
        );

        assertEquals("APPROVED", policyDocument.getStatus(),
            "Status should remain APPROVED");
    }
}

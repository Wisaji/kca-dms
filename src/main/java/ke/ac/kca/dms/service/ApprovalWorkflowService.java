package ke.ac.kca.dms.service;

import ke.ac.kca.dms.model.Document;

/**
 * Manages the approval workflow for documents.
 * Sequence: Initiator -> ApprovalWorkflowService.initiateApproval()
 *           -> Document.setStatus() -> NotificationService.sendNotification()
 *           -> AuditService.logAction()
 */
public class ApprovalWorkflowService {

    private final NotificationService notificationService;
    private final AuditService auditService;

    public ApprovalWorkflowService(NotificationService notificationService,
                                   AuditService auditService) {
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Initiates the approval process for a document.
     * Changes status to IN_REVIEW and notifies the designated approver.
     */
    public void initiateApproval(Document document, String initiatorId,
                                 String approverId) {
        // Step 1: Update document status to IN_REVIEW
        document.setStatus("IN_REVIEW");

        // Step 2: Notify the approver
        notificationService.sendNotification(approverId,
            "Document '" + document.getTitle() +
            "' requires your approval.");

        // Step 3: Log the action in audit trail
        auditService.logAction(initiatorId, "INITIATE_APPROVAL",
            document.getDocumentId(),
            "Approval initiated. Approver: " + approverId);
    }

    /**
     * Approves a document. Status must be IN_REVIEW.
     * Changes status to APPROVED, logs audit, and notifies owner.
     */
    public void approveDocument(Document document, String approverId) {
        if (!"IN_REVIEW".equals(document.getStatus())) {
            throw new IllegalStateException(
                "Document must be IN_REVIEW to approve. Current: "
                + document.getStatus());
        }

        document.setStatus("APPROVED");

        auditService.logAction(approverId, "APPROVE",
            document.getDocumentId(),
            "Document approved by " + approverId);

        notificationService.sendNotification(document.getUploadedBy(),
            "Your document '" + document.getTitle() +
            "' has been approved.");
    }

    /**
     * Rejects a document with a reason. Status must be IN_REVIEW.
     * Changes status to REJECTED, logs audit with reason, notifies initiator.
     */
    public void rejectDocument(Document document, String approverId,
                              String reason) {
        if (!"IN_REVIEW".equals(document.getStatus())) {
            throw new IllegalStateException(
                "Document must be IN_REVIEW to reject. Current: "
                + document.getStatus());
        }

        document.setStatus("REJECTED");

        auditService.logAction(approverId, "REJECT",
            document.getDocumentId(),
            "Document rejected. Reason: " + reason);

        notificationService.sendNotification(document.getUploadedBy(),
            "Your document '" + document.getTitle() +
            "' was rejected. Reason: " + reason);
    }
}
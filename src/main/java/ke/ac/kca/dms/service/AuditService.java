package ke.ac.kca.dms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Audit service that logs all document actions for compliance and traceability.
 */
public class AuditService {

    private final List<AuditEntry> auditLog = new ArrayList<>();

    public void logAction(String userId, String action,
                          String documentId, String details) {
        AuditEntry entry = new AuditEntry(
            userId, action, documentId, details, LocalDateTime.now()
        );
        auditLog.add(entry);
    }

    public List<AuditEntry> getAuditLog() {
        return auditLog;
    }

    public record AuditEntry(
        String userId, String action, String documentId,
        String details, LocalDateTime timestamp
    ) {}
}

// -----------------------------------------------------------------


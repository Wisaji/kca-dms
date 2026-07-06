package ke.ac.kca.dms.service;

public class DocumentService {
    
    public void upload(String docName, String owner) {
        System.out.println("AUDIT: " + owner + " performed UPLOAD on " + docName);
        System.out.println("Document uploaded successfully");
        System.out.println("Document status: DRAFT");
        System.out.println("Total versions: 1");
    }

    public void submitForApproval(String docName, String owner, String office) {
        System.out.println("Notification to " + office + ": Document requires approval: " + docName);
        System.out.println("AUDIT: " + owner + " performed SUBMIT_FOR_APPROVAL on " + docName);
        System.out.println("Status after submission: IN_REVIEW");
    }

    public void approve(String docName, String approver) {
        System.out.println("AUDIT: " + approver + " performed APPROVE on " + docName);
        System.out.println("Status after approval: APPROVED");
    }
}

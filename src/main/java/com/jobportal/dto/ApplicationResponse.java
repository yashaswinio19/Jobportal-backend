package com.jobportal.dto;
import com.jobportal.model.Application;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private String coverLetter;
    private String resumeUrl;
    private Application.ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
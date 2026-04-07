package com.jobportal.service;

import com.jobportal.dto.ApplicationRequest;
import com.jobportal.dto.ApplicationResponse;
import com.jobportal.exception.ResourceAlreadyExistsException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedException;
import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private AuthService authService;

    // ── Apply ─────────────────────────────────────────────────────────────────

    @Transactional
    public ApplicationResponse apply(ApplicationRequest request) {
        User currentUser = authService.getCurrentUser();

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with id: " + request.getJobId()));

        if (job.getStatus() != Job.JobStatus.OPEN) {
            throw new IllegalStateException("This job is no longer accepting applications");
        }

        if (applicationRepository.existsByJobAndApplicant(job, currentUser)) {
            throw new ResourceAlreadyExistsException(
                    "You have already applied for this job");
        }

        Application application = Application.builder()
                .job(job)
                .applicant(currentUser)
                .coverLetter(request.getCoverLetter())
                .resumeUrl(request.getResumeUrl())
                .status(Application.ApplicationStatus.PENDING)
                .build();

        return toResponse(applicationRepository.save(application));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<ApplicationResponse> getMyApplications() {
        User currentUser = authService.getCurrentUser();
        return applicationRepository.findByApplicant(currentUser)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsForJob(Long jobId) {
        User currentUser = authService.getCurrentUser();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with id: " + jobId));

        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(
                    "You are not authorised to view applications for this job");
        }

        return applicationRepository.findByJob(job)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ApplicationResponse getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id: " + id));
        return toResponse(application);
    }

    // ── Update Status ─────────────────────────────────────────────────────────

    @Transactional
    public ApplicationResponse updateStatus(Long id, Application.ApplicationStatus status) {
        User currentUser = authService.getCurrentUser();

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id: " + id));

        if (!application.getJob().getPostedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(
                    "You are not authorised to update this application");
        }

        application.setStatus(status);
        return toResponse(applicationRepository.save(application));
    }

    // ── Withdraw ──────────────────────────────────────────────────────────────

    @Transactional
    public void withdraw(Long id) {
        User currentUser = authService.getCurrentUser();

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id: " + id));

        if (!application.getApplicant().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(
                    "You are not authorised to withdraw this application");
        }

        applicationRepository.delete(application);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ApplicationResponse toResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .company(app.getJob().getCompany())
                .applicantId(app.getApplicant().getId())
                .applicantName(app.getApplicant().getName())
                .applicantEmail(app.getApplicant().getEmail())
                .coverLetter(app.getCoverLetter())
                .resumeUrl(app.getResumeUrl())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
package com.jobportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedException;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AuthService authService;

    // ── Create ──────────────────────────────────────────────────────────────

    @Transactional
    public JobResponse createJob(JobRequest request) {
        User currentUser = authService.getCurrentUser();

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .company(request.getCompany())
                .location(request.getLocation())
                .salary(request.getSalary())
                .category(request.getCategory())
                .jobType(request.getJobType())
                .status(request.getStatus() != null ? request.getStatus() : Job.JobStatus.OPEN)
                .postedBy(currentUser)
                .build();

        return toResponse(jobRepository.save(job));
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public Page<JobResponse> getAllOpenJobs(Pageable pageable) {
        return jobRepository
                .findByStatus(Job.JobStatus.OPEN, pageable)
                .map(this::toResponse);
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return toResponse(job);
    }

    public List<JobResponse> getMyJobs() {
        User currentUser = authService.getCurrentUser();
        return jobRepository.findByPostedBy(currentUser)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<JobResponse> searchJobs(String keyword, Pageable pageable) {
        return jobRepository.searchJobs(keyword, pageable).map(this::toResponse);
    }

    // ── Updated filterJobs with all 4 params ─────────────────────────────────

    public Page<JobResponse> filterJobs(
            String search,
            String location,
            Job.JobType jobType,
            String category,
            Pageable pageable) {

        return jobRepository
                .filterJobs(search, location, jobType, category, pageable)
                .map(this::toResponse);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public JobResponse updateJob(Long id, JobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorised to update this job");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setCategory(request.getCategory());
        job.setJobType(request.getJobType());
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        return toResponse(jobRepository.save(job));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));

        User currentUser = authService.getCurrentUser();
        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorised to delete this job");
        }

        jobRepository.delete(job);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private JobResponse toResponse(Job job) {
        long appCount = applicationRepository.countByJob(job);
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .company(job.getCompany())
                .location(job.getLocation())
                .salary(job.getSalary())
                .category(job.getCategory())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .postedById(job.getPostedBy().getId())
                .postedByName(job.getPostedBy().getName())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .applicationCount((int) appCount)
                .build();
    }
}
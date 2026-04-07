package com.jobportal.controller;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.model.Job;
import com.jobportal.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
public class JobController {

    @Autowired
    private JobService jobService;

    // GET /api/jobs?search=&location=&type=&category=&page=1&limit=6
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Job.JobType jobType = null;
        if (type != null && !type.isBlank()) {
            try { jobType = Job.JobType.valueOf(type.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        Pageable pageable = PageRequest.of(
            Math.max(0, page - 1), limit,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<JobResponse> result = jobService.filterJobs(search, location, jobType, category, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("jobs", result.getContent());
        response.put("total", result.getTotalElements());
        response.put("pages", result.getTotalPages());
        response.put("page", page);

        return ResponseEntity.ok(response);
    }

    // GET /api/jobs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    // GET /api/jobs/my
    @GetMapping("/my")
    public ResponseEntity<List<JobResponse>> getMyJobs() {
        return ResponseEntity.ok(jobService.getMyJobs());
    }

    // POST /api/jobs
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.createJob(request));
    }

    // PUT /api/jobs/{id}
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id, @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    // DELETE /api/jobs/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
package com.jobportal.dto;

import com.jobportal.model.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private String salary;
    private String category;        // ← added
    private Job.JobType jobType;
    private Job.JobStatus status;
    private Long postedById;
    private String postedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int applicationCount;
}
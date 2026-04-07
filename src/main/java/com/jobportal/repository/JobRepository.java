package com.jobportal.repository;

import com.jobportal.model.Job;
import com.jobportal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);

    List<Job> findByPostedBy(User postedBy);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchJobs(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND " +
           "(:search IS NULL OR :search = '' OR " +
           "  LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(j.company) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(j.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:location IS NULL OR :location = '' OR " +
           "  LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:jobType IS NULL OR j.jobType = :jobType) AND " +
           "(:category IS NULL OR :category = '' OR " +
           "  LOWER(j.category) LIKE LOWER(CONCAT('%', :category, '%')))")
    Page<Job> filterJobs(
            @Param("search")   String search,
            @Param("location") String location,
            @Param("jobType")  Job.JobType jobType,
            @Param("category") String category,
            Pageable pageable);
}
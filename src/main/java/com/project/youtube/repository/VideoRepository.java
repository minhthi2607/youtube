package com.project.youtube.repository;

import com.project.youtube.entity.User;
import com.project.youtube.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Page<Video> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Video> findByUploader(User uploader, Pageable pageable);

    @Modifying
    @Query("update Video v set v.views = v.views + 1 where v.id = :id")
    void incrementViews(@Param("id") Long id);
}

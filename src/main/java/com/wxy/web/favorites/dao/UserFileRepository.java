package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.UserFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Integer> {

    List<UserFile> findByPid(Integer pid);

    List<UserFile> findByUserIdAndPidIsNull(Integer userId);

    Page<UserFile> findByUserIdAndFilenameLike(Integer userId, String name, Pageable pageable);

    Page<UserFile> findByUserIdAndPid(Integer userId, Integer pid, Pageable pageable);

    UserFile findByShareId(String shareId);

    UserFile findByPidAndFilename(Integer pid, String filename);

    Long countByUserIdAndIsDirIsNull(Integer userId);
}

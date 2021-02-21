package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Integer> {

    List<UserFile> findByPid(Integer pid);

    List<UserFile> findByUserIdAndPidIsNull(Integer userId);

    List<UserFile> findByUserIdAndFilenameLike(Integer userId,String filename);
}

package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Integer>, JpaSpecificationExecutor<OperationLog> {
}

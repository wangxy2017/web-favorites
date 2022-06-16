package com.wxy.web.favorites.service;

import cn.hutool.core.util.StrUtil;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.dao.OperationLogRepository;
import com.wxy.web.favorites.model.OperationLog;
import com.wxy.web.favorites.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;

    public void save(OperationLog log) {
        operationLogRepository.save(log);
    }

    public PageInfo<OperationLog> findPageList(String name, Integer pageNum, Integer pageSize) {
        String text = SqlUtils.trimAndEscape(name);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "createTime"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(orders));
        // 构造自定义查询条件
        Specification<OperationLog> queryCondition = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (StrUtil.isNotBlank(text)) {
                predicateList.add(criteriaBuilder.like(root.get("name"), "%" + text + "%"));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
        Page<OperationLog> page = operationLogRepository.findAll(queryCondition, pageable);
        return new PageInfo<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }
}

package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.MemorandumRepository;
import com.wxy.web.favorites.model.Memorandum;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class MemorandumService {

    @Autowired
    private MemorandumRepository memorandumRepository;

    public Memorandum save(Memorandum memorandum) {
        return memorandumRepository.save(memorandum);
    }

    public Memorandum findById(Integer id) {
        return memorandumRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        memorandumRepository.deleteById(id);
    }

    public PageInfo<Memorandum> findPageByUserIdAndContentLike(Integer userId, String content, Integer pageNum, Integer pageSize) {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "createTime"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(orders));
        Page<Memorandum> page = memorandumRepository.findPageByUserIdAndContentLike(userId, "%" + SqlUtils.trimAndEscape(content) + "%", pageable);
        return new PageInfo<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    public List<Memorandum> findByUserId(Integer userId) {
        return memorandumRepository.findByUserId(userId);
    }
}


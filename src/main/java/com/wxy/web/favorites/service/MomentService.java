package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.MomentRepository;
import com.wxy.web.favorites.model.Moment;
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
public class MomentService {

    @Autowired
    private MomentRepository momentRepository;

    public void saveAll(List<Moment> momentList) {
        momentRepository.saveAll(momentList);
    }

    public Moment save(Moment moment) {
        return momentRepository.save(moment);
    }

    public Moment findById(Integer id) {
        return momentRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        momentRepository.deleteById(id);
    }

    public Moment findTopMoment(Integer userId) {
        return momentRepository.findTopMoment(userId);
    }

    public PageInfo<Moment> findPageByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "createTime"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(orders));
        Page<Moment> page = momentRepository.findPageByUserId(userId, pageable);
        return new PageInfo<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    public List<Moment> findByUserId(Integer userId) {
        return momentRepository.findByUserId(userId);
    }

    public List<Moment> findMoment(Integer userId, String text) {
        text = SqlUtils.trimAndEscape(text);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "createTime"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        return momentRepository.findByUserIdAndTextLike(userId, "%" + text + "%", Sort.by(orders));
    }

    public int countByUserId(Integer userId) {
        return momentRepository.countByUserId(userId);
    }
}


package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.Moment;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.MomentService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.PageInfo;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/moment")
public class MomentController {

    @Autowired
    private MomentService momentService;

    @Autowired
    private SpringUtils springUtils;

    /**
     * 新增
     *
     * @param moment
     * @return
     */
    @PostMapping
    public ApiResponse save(@RequestBody Moment moment) {
        User user = springUtils.getCurrentUser();
        moment.setUserId(user.getId());
        moment.setCreateTime(new Date());
        momentService.save(moment);
        return ApiResponse.success();
    }

    /**
     * 修改
     *
     * @param moment
     * @return
     */
    @PostMapping("/update")
    public ApiResponse update(@RequestBody Moment moment) {
        Moment moment1 = momentService.findById(moment.getId());
        if (moment1 != null) {
            moment1.setContent(moment.getContent());
            moment1.setText(moment.getText());
            momentService.save(moment1);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Moment moment = momentService.findById(id);
        return ApiResponse.success(moment);
    }

    @GetMapping("/count")
    public ApiResponse count() {
        User user = springUtils.getCurrentUser();
        int count = momentService.countByUserId(user.getId());
        return ApiResponse.success(count);
    }

    @GetMapping("/top")
    public ApiResponse queryTop() {
        User user = springUtils.getCurrentUser();
        Moment moment = momentService.findTopMoment(user.getId());
        return ApiResponse.success(moment);
    }

    @PostMapping("/top/{id}")
    public ApiResponse setTop(@PathVariable Integer id) {
        Moment moment1 = momentService.findById(id);
        if (moment1 != null) {
            // 取消已有置顶
            User user = springUtils.getCurrentUser();
            Moment moment = momentService.findTopMoment(user.getId());
            if (moment != null) {
                moment.setIsTop(0);
                momentService.save(moment);
            }
            // 设置新置顶
            moment1.setIsTop(PublicConstants.MOMENT_TOP_CODE);
            momentService.save(moment1);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @DeleteMapping("/top/{id}")
    public ApiResponse cancelTop(@PathVariable Integer id) {
        Moment moment = momentService.findById(id);
        if (moment != null) {
            moment.setIsTop(0);
            momentService.save(moment);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/search")
    public ApiResponse search(@RequestParam String text) {
        User user = springUtils.getCurrentUser();
        List<Moment> list = momentService.findMoment(user.getId(), text);
        return ApiResponse.success(list);
    }


    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        momentService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse list(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        User user = springUtils.getCurrentUser();
        PageInfo<Moment> page = momentService.findPageByUserId(user.getId(), pageNum, pageSize);
        return ApiResponse.success(page);
    }

}

package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.Moment;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.MomentService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.PageInfo;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/moment")
public class MomentController {

    @Autowired
    private MomentService momentService;

    @Autowired
    private SpringUtils springUtils;

    @PostMapping
    public ApiResponse save(@RequestBody Moment moment) {
        User user = springUtils.getCurrentUser();
        moment.setUserId(user.getId());
        momentService.save(moment);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Moment moment = momentService.findById(id);
        return ApiResponse.success(moment);
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

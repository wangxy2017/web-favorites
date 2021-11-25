package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.Memorandum;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.MemorandumService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.PageInfo;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/memorandum")
public class MemorandumController {

    @Autowired
    private MemorandumService memorandumService;

    @Autowired
    private SpringUtils springUtils;

    /**
     * 新增
     *
     * @param memorandum
     * @return
     */
    @PostMapping
    public ApiResponse save(@RequestBody Memorandum memorandum) {
        User user = springUtils.getCurrentUser();
        memorandum.setUserId(user.getId());
        memorandum.setCreateTime(new Date());
        memorandumService.save(memorandum);
        return ApiResponse.success();
    }

    /**
     * 修改
     *
     * @param memorandum
     * @return
     */
    @PostMapping("/update")
    public ApiResponse update(@RequestBody Memorandum memorandum) {
        Memorandum Memorandum1 = memorandumService.findById(memorandum.getId());
        if (Memorandum1 != null) {
            Memorandum1.setContent(memorandum.getContent());
            memorandumService.save(Memorandum1);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Memorandum memorandum = memorandumService.findById(id);
        return ApiResponse.success(memorandum);
    }

    @GetMapping("/list")
    public ApiResponse list(@RequestParam(required = false) String content, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        User user = springUtils.getCurrentUser();
        PageInfo<Memorandum> page = memorandumService.findPageByUserIdAndContentLike(user.getId(), content, pageNum, pageSize);
        return ApiResponse.success(page);
    }

}

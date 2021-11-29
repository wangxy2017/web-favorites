package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.Memorandum;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.MemorandumService;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.security.ContextUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/memorandum")
@Api(tags = "备忘录")
public class MemorandumController {

    @Autowired
    private MemorandumService memorandumService;

    @Autowired
    private ContextUtils contextUtils;

    /**
     * 新增
     *
     * @param memorandum
     * @return
     */
    @PostMapping
    @ApiOperation(value = "保存")
    public ApiResponse save(@RequestBody Memorandum memorandum) {
        User user = contextUtils.getCurrentUser();
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
    @ApiOperation(value = "修改")
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
    @ApiOperation(value = "根据id查询")
    public ApiResponse query(@PathVariable Integer id) {
        Memorandum memorandum = memorandumService.findById(id);
        return ApiResponse.success(memorandum);
    }

    @GetMapping("/list")
    @ApiOperation(value = "分页查询")
    public ApiResponse list(@RequestParam(required = false) String content, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        User user = contextUtils.getCurrentUser();
        PageInfo<Memorandum> page = memorandumService.findPageByUserIdAndContentLike(user.getId(), content, pageNum, pageSize);
        return ApiResponse.success(page);
    }

}

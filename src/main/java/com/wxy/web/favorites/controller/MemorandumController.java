package com.wxy.web.favorites.controller;

import cn.hutool.core.lang.Assert;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.model.Memorandum;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.security.SecurityUser;
import com.wxy.web.favorites.service.MemorandumService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/memorandum")
@Api(tags = "备忘录")
@Secured("memorandum")
public class MemorandumController {

    @Autowired
    private MemorandumService memorandumService;

    /**
     * 新增
     *
     * @param memorandum
     * @return
     */
    @PostMapping
    @ApiOperation(value = "保存")
    public ApiResponse save(@RequestBody Memorandum memorandum) {
        Assert.notBlank(memorandum.getContent(),"内容不能为空");
        if (memorandum.getId() == null) {// 新建
            SecurityUser user = ContextUtils.getCurrentUser();
            memorandum.setUserId(user.getId());
            memorandum.setCreateTime(new Date());
            memorandumService.save(memorandum);
        } else {// 修改
            Memorandum memorandum1 = memorandumService.findById(memorandum.getId());
            Assert.notNull(memorandum1, "备忘录不存在");
            memorandum1.setContent(memorandum.getContent());
            memorandumService.save(memorandum1);
        }
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询")
    public ApiResponse query(@PathVariable Integer id) {
        Memorandum memorandum = memorandumService.findById(id);
        return ApiResponse.success(memorandum);
    }

    @GetMapping("/search")
    @ApiOperation(value = "搜索备忘录")
    public ApiResponse search(@RequestParam String content) {
        SecurityUser user = ContextUtils.getCurrentUser();
        List<Memorandum> list = memorandumService.findMemorandum(user.getId(), content);
        return ApiResponse.success(list);
    }

    @GetMapping("/list")
    @ApiOperation(value = "分页查询")
    public ApiResponse list(@RequestParam(required = false) String content, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        SecurityUser user = ContextUtils.getCurrentUser();
        PageInfo<Memorandum> page = memorandumService.findPageByUserIdAndContentLike(user.getId(), content, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @GetMapping("/count")
    @ApiOperation(value = "统计我的备忘录")
    public ApiResponse count() {
        SecurityUser user = ContextUtils.getCurrentUser();
        long count = memorandumService.countByUserId(user.getId());
        return ApiResponse.success(count);
    }

    @GetMapping("/delete/{id}")
    @ApiOperation(value = "删除备忘录")
    public ApiResponse delete(@PathVariable Integer id) {
        memorandumService.deleteById(id);
        return ApiResponse.success();
    }

}

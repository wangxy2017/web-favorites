package com.wxy.web.favorites.controller;

import cn.hutool.core.lang.Assert;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.Moment;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.security.SecurityUser;
import com.wxy.web.favorites.service.MomentService;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.security.ContextUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/moment")
@Api(tags = "瞬间")
@Secured("moment")
public class MomentController {

    @Autowired
    private MomentService momentService;

    /**
     * 新增
     *
     * @param moment
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增瞬间")
    public ApiResponse save(@RequestBody Moment moment) {
        Assert.notBlank(moment.getContent(),"内容不能为空");
        SecurityUser user = ContextUtils.getCurrentUser();
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
    @ApiOperation(value = "修改瞬间")
    public ApiResponse update(@RequestBody Moment moment) {
        Assert.notBlank(moment.getContent(),"内容不能为空");
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
    @ApiOperation(value = "根据id查询")
    public ApiResponse query(@PathVariable Integer id) {
        Moment moment = momentService.findById(id);
        return ApiResponse.success(moment);
    }

    @GetMapping("/count")
    @ApiOperation(value = "统计我的瞬间")
    public ApiResponse count() {
        SecurityUser user = ContextUtils.getCurrentUser();
        int count = momentService.countByUserId(user.getId());
        return ApiResponse.success(count);
    }

    @GetMapping("/top")
    @ApiOperation(value = "查询置顶瞬间")
    public ApiResponse queryTop() {
        SecurityUser user = ContextUtils.getCurrentUser();
        Moment moment = momentService.findTopMoment(user.getId());
        return ApiResponse.success(moment);
    }

    @PostMapping("/top/{id}")
    @ApiOperation(value = "置顶")
    public ApiResponse setTop(@PathVariable Integer id) {
        Moment moment1 = momentService.findById(id);
        if (moment1 != null) {
            // 取消已有置顶
            SecurityUser user = ContextUtils.getCurrentUser();
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
    @ApiOperation(value = "取消置顶")
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
    @ApiOperation(value = "搜索瞬间")
    public ApiResponse search(@RequestParam String text) {
        SecurityUser user = ContextUtils.getCurrentUser();
        List<Moment> list = momentService.findMoment(user.getId(), text);
        return ApiResponse.success(list);
    }


    @GetMapping("/delete/{id}")
    @ApiOperation(value = "删除瞬间")
    public ApiResponse delete(@PathVariable Integer id) {
        momentService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "分页查询")
    public ApiResponse list(@RequestParam(required = false) String text,@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        SecurityUser user = ContextUtils.getCurrentUser();
        PageInfo<Moment> page = momentService.findPageByUserIdAndTextLike(user.getId(),text, pageNum, pageSize);
        return ApiResponse.success(page);
    }

}

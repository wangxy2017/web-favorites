package com.wxy.web.favorites.controller.admin;

import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.dao.OperationLogRepository;
import com.wxy.web.favorites.model.OperationLog;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.OperationLogService;
import com.wxy.web.favorites.util.JpaUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author e-Xiaoyuan.Wang
 * @since 2022/6/15 10:09
 */
@RestController
@RequestMapping("/operation-log")
@Api(tags = "操作日志")
@Secured("operation_log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    @ApiOperation(value = "获取管理员列表")
    public ApiResponse list(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Integer pageNum,
                            @RequestParam(required = false) Integer pageSize) {
        PageInfo<OperationLog> page = operationLogService.findPageList(name, pageNum, pageSize);
        return ApiResponse.success(page);
    }
}

package com.wxy.web.favorites.controller.admin;

import io.swagger.annotations.Api;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

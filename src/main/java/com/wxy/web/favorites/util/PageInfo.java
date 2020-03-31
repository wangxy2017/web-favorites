package com.wxy.web.favorites.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @Author HL
 * @Date 2020/3/31 17:16
 * @Description TODO
 **/
@Data
@AllArgsConstructor
public class PageInfo<T> {
    private List<T> list;
    private Integer pages;
    private Long total;
}

layui.use(['element', 'layer', 'table'], function() {
        var element = layui.element;
        var layer = layui.layer;
        var table = layui.table;

        //加载数据
        table.render({
            elem: '#favoritesList'
            , url: 'favorites/recycle/' //数据接口
            , headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            , page: {
                layout: ['count', 'prev', 'page', 'next', 'skip']
                , prev: '上一页'
                , next: '下一页'
                , limit: 5
            }
            , request: {
                pageName: 'pageNum' //页码的参数名称，默认：page
                , limitName: 'pageSize' //每页数据量的参数名，默认：limit
            }
            , parseData: function (res) { //res 即为原始返回的数据
                return {
                    "code": res.code, //解析接口状态
                    "msg": res.msg, //解析提示文本
                    "count": res.data.total,
                    "data": res.data.list //解析数据列表
                };
            }
            , done: function (res, curr, count) {
                // 解决无数据table样式错乱
                if(count == 0){
                    $("th[data-key='1-0-4']").css("border-right", "0");
                }
            }
            , cols: [[ //表头
                {type: 'numbers'}
                , {
                    field: 'name', title: '名称', minWidth: 200, templet: function (d) {
                        return '<span>' + escape(d.name) + '</span>';
                    }
                }
                , {
                    field: 'url', title: '地址', templet: function (d) {
                        return '<a class="layui-blue" href="javascript:openUrl(\'' + d.url + '\');">' + d.url + '</a>'
                    }
                }
                , {
                    field: 'deleteTime', title: '删除时间'
                }
                , {title: '操作', width: 120, toolbar: '#operates', fixed: 'right'}
            ]]
        });

        //监听工具条
        table.on('tool(favoritesList)', function (obj) {
            var data = obj.data;
            var layEvent = obj.event;
            if (layEvent === 'recover') { //还原
                $.ajax({
                    type: "GET",
                    url: "favorites/recover/" + data.id,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            obj.del(); //移除当前行
                            layer.msg('操作成功', {icon: 6});
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
            } else if (layEvent === 'del') { //删除
                $.ajax({
                    type: "POST",
                    url: "favorites/recycle/delete/" + data.id,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            obj.del(); //移除当前行
                            layer.msg('删除成功', {icon: 6});
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
            }
        });

        //加载数据
        table.render({
            elem: '#shareList'
            , url: 'favorites/shareList/' //数据接口
            , headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            , page: {
                layout: ['count', 'prev', 'page', 'next', 'skip']
                , prev: '上一页'
                , next: '下一页'
                , limit: 5
            }
            , request: {
                pageName: 'pageNum' //页码的参数名称，默认：page
                , limitName: 'pageSize' //每页数据量的参数名，默认：limit
            }
            , parseData: function (res) { //res 即为原始返回的数据
                return {
                    "code": res.code, //解析接口状态
                    "msg": res.msg, //解析提示文本
                    "count": res.data.total,
                    "data": res.data.list //解析数据列表
                };
            }
            , done: function (res, curr, count) {
                // 解决无数据table样式错乱
                if(count == 0){
                    $("th[data-key='2-0-4']").css("border-right", "0");
                }
            }
            , cols: [[ //表头
                {type: 'numbers'}
                , {
                    field: 'name', title: '名称', minWidth: 200, templet: function (d) {
                        return '<span>' + escape(d.name) + '</span>';
                    }
                }
                , {
                    field: 'url', title: '地址', templet: function (d) {
                        return '<a class="layui-blue" href="javascript:openUrl(\'' + d.url + '\');">' + d.url + '</a>'
                    }
                }
                , {
                    field: 'support', title: '收藏数', align: 'center', width: 100, templet: function (d) {
                        return transform(d.support);
                    }
                }
                , {title: '操作', width: 90, toolbar: '#shareOperates', fixed: 'right'}
            ]]
        });

        //监听工具条
        table.on('tool(shareList)', function (obj) {
            var data = obj.data;
            var layEvent = obj.event;
            if (layEvent === 'no-share') { //还原
                $.ajax({
                    type: "GET",
                    url: "favorites/no-share/" + data.id,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            obj.del(); //移除当前行
                            layer.msg('操作成功', {icon: 6});
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
            }
        });

        // 打开新窗口
        window.openUrl = function (url) {
            if(url.indexOf("https://") == 0 || url.indexOf("http://") == 0){
                newWin(url);
                // 记录访问次数
                $.ajax({
                    type: "GET",
                    url: "user/visit",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
                });
            }else{
                layer.msg('此链接无效', {icon: 7});
            }
        };




        $(".tab-title li").click(function(){
            $(this).addClass("active").siblings().removeClass("active");
            $(".tab-content").children().eq($(this).index()).show().siblings().hide();
        });

        $("#cleanRecycle").click(function(){
            parent.layer.confirm('确认清空回收站吗?', function(index){
              parent.layer.close(index);

                $.ajax({
                    type: "POST",
                    url: "favorites/recycle/clean/",
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            layer.msg('操作成功', {icon: 6});
                            table.reload('favoritesList');
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
            });
        });


    });
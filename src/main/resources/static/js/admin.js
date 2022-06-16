layui.use(['element', 'layer', 'table', 'form'], function() {
    var element = layui.element;
    var layer = layui.layer;
    var table = layui.table;
    var form = layui.form;

    //加载数据
    table.render({
        elem: '#adminList'
        , url: 'admin/list/' //数据接口
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
            $("#superAdmin").parent().parent().parent().find(".layui-btn").addClass("layui-btn-disabled");
        }
        , cols: [[ //表头
            {type: 'numbers'}
            , {
                field: 'username', title: '账号', templet: function (d) {
                    var id = d.superAdmin ? 'id="superAdmin"' : '';
                    return '<span '+ id +'>'+ d.username +'</span>';
                }
            }
            , {field: 'nickName', title: '姓名'}
            , {field: 'registerTime', title: '创建时间'}
            , {field: 'lastOnlineTime', title: '上次在线时间'}
            , {
                field: 'status', title: '状态', width: 80, templet: function (d) {
                    var text = d.status == 2 ? '禁用' : '正常';
                    var class_ = d.status == 2 ? 'layui-bg-orange' : 'layui-bg-green';
                    return '<span class="layui-badge '+ class_ +'">'+ text +'</span>'
                }
            }
            , {title: '操作', width: 220, toolbar: '#operates', fixed: 'right'}
        ]]
    });

    //监听工具条
    table.on('tool(adminList)', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;
        if (layEvent === 'disable') {
            $.ajax({
                type: "GET",
                url: "admin/disable/" + data.id,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        layer.msg('操作成功', {icon: 6});
                        table.reload('adminList');
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
        } else if (layEvent === 'del') {
            layer.confirm('确认删除账号吗?', function(index){
                layer.close(index);
                $.ajax({
                    type: "DELETE",
                    url: "admin/delete/" + data.id,
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
            });
        } else if (layEvent === 'updatePwd') {
            layer.prompt({formType: 1, title: "重置密码", placeholder:"请输入新密码", maxlength: 20}, function(value, index, elem){
                $.ajax({
                    type: "POST",
                    url: "admin/updatePwd",
                    contentType: 'application/json;charset=utf-8',
                    data: JSON.stringify({"id": data.id, "password": md5(value)}),
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            layer.msg('修改成功', {icon: 6});
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
              layer.close(index);
            });
        }
    });

    // 添加管理员
    $("#addAdminBtn").click(function () {
        var index = layer.open({
            type: 1,
            title: "添加管理员",
            content: $('#add-admin')
        });
        indexMap.set('#add-admin',index);
        // 表单赋值
        form.val("add-admin", {
            "nickName": ""
            , "username": ""
            , "password": ""
        });
    });

    $("#cancelAddAdmin").click(function () {
        layer.close(indexMap.get('#add-admin'));
    });

    form.on('submit(addAdmin)', function (data) {
        data.field.password = md5(data.field.password);
        layer.load();
        $.ajax({
            type: "POST",
            url: "admin/save",
            data: JSON.stringify(data.field),
            contentType: 'application/json;charset=utf-8',
            dataType: "json",
            headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
            success: function (result) {
                layer.closeAll('loading');
                layer.close(indexMap.get('#add-admin'));
                if (result.code == 0) {
                    table.reload('adminList');
                } else {
                    layer.msg(result.msg, {icon: 5});
                }
            }
        });
        return false;
    });

    initSearch("#searchName");

    // 搜索
    $('#searchName').bind('keypress', function (event) {
        if (event.key === "Enter") {
            $("#search").click();
        }
    });

    $("#search").click(function(){
        var name = $("#searchName").val();
        table.reload('adminList', {page: {curr: 1}, where: {"name": name}});
    });
});
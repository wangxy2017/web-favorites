layui.use(['element', 'layer', 'table'], function() {
    var element = layui.element;
    var layer = layui.layer;
    var table = layui.table;

    //加载数据
    table.render({
        elem: '#userList'
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
        , cols: [[ //表头
            {type: 'numbers'}
            , {field: 'username', title: '账号'}
            , {field: 'nickName', title: '姓名'}
            , {
                field: 'status', title: '状态', templet: function (d) {
                    var text = d.status == 2 ? '禁用' : '正常';
                    var class = d.status == 2 ? 'layui-bg-orange' : 'layui-bg-green';
                    return '<span class="layui-badge '+ class +''">'+ text +'</span>'
                }
            }
            , {title: '操作', width: 220, toolbar: '#operates', fixed: 'right'}
        ]]
    });

    //监听工具条
    table.on('tool(userList)', function (obj) {
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
                        layer.msg('操作失败', {icon: 5});
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
                        layer.msg('删除失败', {icon: 5});
                    }
                }
            });
        }
    });
});
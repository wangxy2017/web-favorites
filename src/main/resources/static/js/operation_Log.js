layui.use(['element', 'layer', 'table'], function() {
    var element = layui.element;
    var layer = layui.layer;
    var table = layui.table;

    //加载数据
    table.render({
        elem: '#logList'
        , toolbar: true
        , url: 'operation-log/list/' //数据接口
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
            , {field: 'name', title: '姓名'}
            , {field: 'content', title: '内容'}
            , {field: 'module', title: '模块'}
            , {field: 'createTime', title: '操作时间'}
        ]]
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
        table.reload('logList', {page: {curr: 1}, where: {"name": name}});
    });
});
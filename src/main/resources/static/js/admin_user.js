layui.use(['element', 'layer', 'table'], function() {
    var element = layui.element;
    var layer = layui.layer;
    var table = layui.table;

    //加载数据
    table.render({
        elem: '#userList'
        , url: 'admin-user/list/' //数据接口
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
            , {field: 'nickName', title: '昵称'}
            , {field: 'email', title: '邮箱'}
            , {field: 'registerTime', title: '注册时间'}
            , {field: 'lastOnlineTime', title: '上次在线时间'}
            , {
                field: 'status', title: '状态', width: 80, templet: function (d) {
                    var text = d.status == 2 ? '禁用' : '正常';
                    var class_ = d.status == 2 ? 'layui-bg-orange' : 'layui-bg-green';
                    return '<span class="layui-badge '+ class_ +'">'+ text +'</span>'
                }
            }
            , {title: '操作', width: 240, toolbar: '#operates', fixed: 'right'}
        ]]
    });

    //监听工具条
    table.on('tool(userList)', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;
        if (layEvent === 'disable') {
            $.ajax({
                type: "GET",
                url: "admin-user/disable/" + data.id,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        layer.msg('操作成功', {icon: 6});
                        table.reload('userList');
                    } else {
                        layer.msg('操作失败', {icon: 5});
                    }
                }
            });
        } else if (layEvent === 'clean') {
            $.ajax({
                type: "GET",
                url: "admin-user/clean/" + data.id,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        layer.msg('清除成功', {icon: 6});
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
        } else if (layEvent === 'sendMail') {
             var width = (windowWidth >= 800? 800 : windowWidth) + 'px';
             layer.open({
               id:"wangEditor",
               type: 2,
               title: "发送邮件",
               content: 'send_mail_edit.html?' + timeSuffix(),
               area: [width,'500px'],
               btn: ['发送', '取消'],
               yes: function(index, layero){
                 var frameId = $(layero).find("iframe").attr('id');
                 var subWindow = document.getElementById(frameId).contentWindow;
                 var html = subWindow.editor.txt.html();
                 var text = subWindow.editor.txt.text();
                 if(html && text){
                      if(text.length > 20000){
                          layer.msg('最多输入20000个字符');
                          return false;
                      }
                      // 保存数据
                      layer.load();
                      $.ajax({
                          type: "POST",
                          url: "admin-user/sendMail",
                          data: {"content": html, "id": data.id},
                          dataType: "json",
                          headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                          success: function (result) {
                              layer.closeAll('loading');
                              if (result.code == 0) {
                                  layer.msg('发送成功', {icon: 6});
                              } else {
                                  layer.msg(result.msg, {icon: 5});
                              }
                          }
                      });
                  }else{
                      layer.msg("内容不能为空");
                  }
               }
               });
        }
    });

    $(document).on("keydown", function(event){
        if(event.ctrlKey && event.key === "f"){
            $("#searchName").focus();
            // 阻止默认浏览器动作(W3C)
            var e = event;
            if ( e && e.preventDefault )
                e.preventDefault();
            // IE中阻止函数器默认动作的方式
            else
                window.event.returnValue = false;
            return false;
        }
    });

    // 搜索
    $('#searchName').bind('keypress', function (event) {
        if (event.key === "Enter") {
            $("#search").click();
        }
    });

    $("#search").click(function(){
        var name = $("#searchName").val();
        table.reload('userList', {page: {curr: 1}, where: {"name": name}});
    });
});
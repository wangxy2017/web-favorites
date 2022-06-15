//JavaScript代码区域
layui.use(['element', 'layer'], function () {
    var element = layui.element;
    var layer = layui.layer;

    $.ajax({
        type: "GET",
        url: "index/info",
        dataType: "json",
        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
        success: function (result) {
            if (result.code == 0) {
                var user = result.data;
                $("#username").text(user.nickName.substring(0, 4));
            }
        }
    });

    element.on('nav(admin-menu)', function(elem){
      console.log(elem); //得到当前点击的DOM对象
    });

    // 登出
    $("#logout").click(function () {
        layer.confirm('确认退出系统吗？', function(index){
            layer.close(index);

            localStorage.clear();
            window.location.href = "login.html";
        });
    });
});
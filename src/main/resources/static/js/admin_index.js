//JavaScript代码区域
layui.use(['element', 'layer'], function () {
    var element = layui.element;
    var layer = layui.layer;

    $("#slideBar").click(function(){
        if(!$(this).parent().parent().hasClass("layui-mobile")){
            $(".layui-side").addClass("layui-mobile");
            $(".layui-body").addClass("layui-mobile");
            $(".layui-footer").addClass("layui-mobile");
            $(".layui-logo").addClass("layui-mobile");
            $(".layui-nav.layui-layout-left").addClass("layui-mobile");
        }else{
            $(".layui-side").removeClass("layui-mobile");
            $(".layui-body").removeClass("layui-mobile");
            $(".layui-footer").removeClass("layui-mobile");
            $(".layui-logo").removeClass("layui-mobile");
            $(".layui-nav.layui-layout-left").removeClass("layui-mobile");
        }
    });

    if(windowWidth < 800){
        $("#slideBar").click();
    };

    $.ajax({
        type: "GET",
        url: "admin-index/info",
        dataType: "json",
        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
        success: function (result) {
            if (result.code == 0) {
                var user = result.data;
                $("#username").text(user.nickName.substring(0, 4));
                var permissions = user.permissions;
                $("#adminMenu").find("[data-permission]").each(function(i){
                    if(permissions.contains($(this).attr("data-permission"))){
                        $(this).parent().show();
                    }else{
                        $(this).parent().hide();
                    }
                });
            }
        }
    });

    element.on('nav(admin-menu)', function(elem){
        var url = $(elem).attr("data-href") + '?' + timeSuffix();
        $("#iframe").attr("src",url);
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
layui.use(['element', 'layer', 'form'], function() {
    var element = layui.element;
    var layer = layui.layer;
    var form = layui.form;

    $.ajax({
        type: "GET",
        url: "user/info",
        dataType: "json",
        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
        success: function (result) {
            if (result.code == 0) {
                var user = result.data;
                form.val("emailForm", {"nickName": user.nickName, "email": user.email});
            }
        }
    });

    // 修改邮箱
    form.on('submit(updateEmail)', function (data) {
        layer.load();
        $.ajax({
            type: "POST",
            url: "user/update",
            data: data.field,
            dataType: "json",
            headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
            success: function (result) {
                layer.closeAll('loading');
                if (result.code == 0) {
                    layer.msg("保存成功", {icon: 6});
                } else {
                    layer.msg(result.msg, {icon: 5});
                }
            }
        });
        return false;
    });

    // 修改密码
    form.on('submit(updatePassword)', function (data) {
        layer.load();
        $.ajax({
            type: "POST",
            url: "user/password",
            data: {"oldPassword":md5(data.field.oldPassword),"newPassword":md5(data.field.newPassword)},
            dataType: "json",
            headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
            success: function (result) {
                layer.closeAll('loading');
                if (result.code == 0) {
                    layer.msg('保存成功', {icon: 6, time: 1000}, function(){
                        localStorage.clear();
                        parent.window.location.href = "login.html";
                    });
                } else {
                    layer.msg(result.msg, {icon: 5});
                }
            }
        });
        return false;
    });

    form.verify({
        confirmPassword: function (value, item) {
            if($("#newPassword").val() !== value){
                return "两次输入密码不一致";
            }
        }
    });

    // 发送验证码
    window.sendEmailCode = function (obj) {
        var email = $("#email").val();
        var reg = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
        if(email.match(reg)){
            obj.setAttribute("disabled", true);
            obj.value="重试(60)";
            // 倒计时
            var countdown = 59;
            var interval = setInterval(function() {
                if (countdown == 0) {
                    obj.removeAttribute("disabled");
                    obj.value="点击获取";
                    countdown = 59;
                    clearInterval(interval);
                } else {
                    obj.setAttribute("disabled", true);
                    obj.value="重试(" + countdown + ")";
                    countdown--;
                }
            }, 1000);
            // 请求后台
            $.ajax({
                type: "GET",
                url: "user/email/code",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                data: {"email": email}
            });
        }else{
            $("#email").focus().addClass("layui-form-danger");
            layer.msg('邮箱格式不正确', {icon: 5, anim: 6});
        }
    };
});
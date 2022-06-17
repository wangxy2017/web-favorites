layui.use(['form','layer'], function(){
        var form = layui.form;
        var layer = layui.layer;

        layer.ready(function(){
            var username = localStorage.getItem("qr_login_username");
            var password = localStorage.getItem("qr_login_password");
            var url = window.location.href;
            var sid = url.lastIndexOf("sid=") == -1 ? null : url.substring(url.lastIndexOf("sid=") + 4);
            if(username&&password&&sid){
                $.ajax({
                    type: "POST",
                    url: "login/qrLogin",
                    data: JSON.stringify({"username":username,"password":password,"sid":sid}),
                    contentType: 'application/json;charset=utf-8',
                    dataType: "json",
                    success: function (result) {
                        if (result.code == 0) {
                            $("#loginBox").hide();
                            $("#loginDone").show();
                        }
                    }
                });
            }
        });

        //监听提交
        form.on('submit(login)', function (data) {
            var url = window.location.href;
            if(url.lastIndexOf("sid=") == -1){
                layer.msg("缺少必要参数：sid");
                return false;
            }
            layer.load();
            // sid
            var sid = url.substring(url.lastIndexOf("sid=") + 4);
            data.field.sid = sid;
            // md5加密
            data.field.password = md5(data.field.password);
            // 记住密码
            if(data.field.remember){
                localStorage.setItem("qr_login_username", data.field.username);
                localStorage.setItem("qr_login_password", data.field.password);
            }
            $.ajax({
                type: "POST",
                url: "login/qrLogin",
                data: JSON.stringify(data.field),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                success: function (result) {
                    layer.closeAll('loading');
                    if (result.code == 0) {
                        $("#loginBox").hide();
                        $("#loginDone").show();
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                },
                error: function () {
                    layer.closeAll('loading');

                }
            });
            return false;
        });
    });
//注意：选项卡 依赖 element 模块，否则无法进行功能性操作
layui.use(['element', 'form', 'layer'], function () {
    var element = layui.element;
    var form = layui.form;
    var layer = layui.layer;

    var socketPort = 8889;

    if(windowWidth < 800){
        $(".loginDiv").width("360px");
    }

    /* particlesJS.load(@dom-id, @path-json, @callback (optional)); */
    particlesJS.load('particles-js', 'plugin/particles.json');

    var verifyCodeTimer;
    window.loadVerifyCode = function(){
        $.ajax({
            type: "GET",
            url: 'login/captcha',
            dataType: 'json',
            success: function (result) {
                if (result.code == 0) {
                    $("#sid").val(result.data.key);
                    $("#code").val("");
                    $("#code").next().remove();
                    $("#code").after('<img class="verifyCode" src="' + result.data.image + '" alt="验证码" onclick="loadVerifyCode()">');
                    clearInterval(verifyCodeTimer);
                    verifyCodeTimer = setInterval(loadVerifyCode, 60000);
                }
            }
        });
    };

    loadVerifyCode();

    // 刷新二维码
    var refreshTime;
    $("#refreshCode").click(function(){
        var now = new Date().getTime();
        if(refreshTime && now - refreshTime < 5000){
            layer.tips('刷新频繁,稍后再试', this, {tips: 2});
            return false;
        }
        refreshTime = now;
        // 初始化二维码
        var sid = uuid();
        initQrCode(sid);
        initWebSocket(sid);
    });

    window.uuid = function() {
        var s = [];
        var hexDigits = "0123456789abcdef";
        for (var i = 0; i < 36; i++) {
            s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
        }
        s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
        s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
        s[8] = s[13] = s[18] = s[23] = "-";

        var uuid = s.join("");
        return uuid;
    };

    var qrcode;
    window.initQrCode = function(sid){
        var url = window.location.href;
        var path = url.substring(0,url.lastIndexOf("/"));
        var content = path + "/qrLogin.html?sid=" + sid;
        if(!qrcode){
            qrcode = new QRCode("qrcode", {
                text: content,
                width: 180,
                height: 180,
                colorDark : "#000000",
                colorLight : "#ffffff",
                correctLevel : QRCode.CorrectLevel.H
            });
        }else{
            qrcode.clear();
            qrcode.makeCode(content);
        }
    };

    // 邮箱登录
    window.emailLogin = function(obj){
        $("#loginForm").hide();
        $("#qrcodeDiv").hide();
        $("#emailLogin").show();
        $(obj).parent().hide().next().show();
    };

    // 切换登录方式
    window.changeLogin = function(obj){
        $("#emailLogin").hide();
        $("#qrcodeDiv").hide();
        $("#loginForm").show();
        $(obj).parent().hide().prev().show();
    };

    // 扫码登录
    window.qrcodeLogin = function(obj){
        $("#loginForm").hide();
        $("#emailLogin").hide();
        $("#qrcodeDiv").show();
        $(obj).parent().hide().next().show();
        // 初始化二维码
        var sid = uuid();
        initQrCode(sid);
        initWebSocket(sid);
    };

    var socket;
    window.initWebSocket = function(sid){
       if(typeof(WebSocket) == "undefined") {
           console.log("您的浏览器不支持WebSocket");
       }else{
           if(socket){ socket.close(); }
           socket = new WebSocket("ws://" + window.location.host.split(':')[0] + ":" + socketPort + "/websocket?sid=" + sid);
           //打开事件
           socket.onopen = function() {
               console.log("Socket 已打开");
           };
           //获得消息事件
           socket.onmessage = function(msg) {
               var result = JSON.parse(msg.data);
               if(result.code == 0){
                   localStorage.setItem("login_user_token", result.data);
                   window.location.href = "index.html";
               }
           };
           //关闭事件
           socket.onclose = function() {
               console.log("Socket已关闭");
           };
           //发生了错误事件
           socket.onerror = function() {
               alert("Socket发生了错误");
           }
       }
    };

    //监听提交
    form.on('submit(emailLogin)', function (data) {
        layer.load();
        $.ajax({
            type: "POST",
            url: "login/emailLogin",
            data: data.field,
            dataType: "json",
            success: function (result) {
                layer.closeAll('loading');
                if (result.code == 0) {
                    localStorage.setItem("login_user_token", result.data);
                    window.location.href = "index.html";
                } else {
                    layer.msg(result.msg, {icon: 5});
                }
            },
            error: function () {
                layer.closeAll('loading');
                layer.msg("服务器异常", {icon: 2});
            }
        });
        return false;
    });

    //监听提交
    form.on('submit(login)', function (data) {
        layer.load();
        // md5加密
        data.field.password = md5(data.field.password);
        $.ajax({
            type: "POST",
            url: "login?remember=" + data.field.remember,
            data: JSON.stringify(data.field),
            contentType: 'application/json;charset=utf-8',
            dataType: "json",
            success: function (result) {
                layer.closeAll('loading');
                if (result.code == 0) {
                    var data = result.data;
                    localStorage.setItem("login_user_token", data.accessToken);
                    window.location.href = data.admin ? "admin_index.html" : "index.html";
                } else {
                    layer.msg(result.msg, {icon: 5});
                    loadVerifyCode();
                }
            },
            error: function () {
                layer.closeAll('loading');
                layer.msg("服务器异常", {icon: 2});
            }
        });
        return false;
    });

    form.on('submit(forgotPwd)', function (data) {
        layer.load();
        $.ajax({
            type: "POST",
            url: "login/forgot",
            data: JSON.stringify(data.field),
            contentType: 'application/json;charset=utf-8',
            dataType: "json",
            success: function (result) {
                layer.closeAll('loading');
                if (result.code == 0) {
                    layer.msg("新密码已发送至您的邮箱...", {icon: 6});
                } else {
                    layer.msg(result.msg, {icon: 5});
                }
            },
            error: function () {
                layer.closeAll('loading');
                layer.msg("服务器异常", {icon: 2});
            }
        });
        return false;
    });

    form.on('submit(register)', function (data) {
        layer.load();
        // md5加密
        data.field.password = md5(data.field.password);
        $.ajax({
            type: "POST",
            url: "register",
            data: JSON.stringify(data.field),
            contentType: 'application/json;charset=utf-8',
            dataType: "json",
            success: function (result) {
                layer.closeAll('loading');
                if (result.code == 0) {
                    localStorage.setItem("login_user_token", result.data);
                    window.location.href = "index.html";
                } else {
                    layer.msg(result.msg, {icon: 5});
                }
            },
             error: function () {
                layer.closeAll('loading');
                layer.msg("服务器异常", {icon: 2});
            }
        });
        return false;
    });

    form.verify({
        username: function (value, item) {
            if(value.length<6||value.length>18||!/^[0-9a-zA-Z_]{1,}$/.test(value)){
                return '输入6~18个字符，字母、数字、下划线';
            }
            var msg;
            $.ajax({
                type: "GET",
                url: 'register/' + value,
                async: false,
                dataType: 'json',
                success: function (result) {
                    if (result.code != 0) {
                        msg = "用户已注册";
                    }
                }
            });
            return msg;
        },
        exitEmail: function (value, item) {
            var msg;
            $.ajax({
                type: "GET",
                url: 'register/email/' + value,
                async: false,
                dataType: 'json',
                success: function (result) {
                    if (result.code != 0) {
                        msg = "邮箱已被使用";
                    }
                }
            });
            return msg;
        },
        password: function (value, item) {
            if(value.length<6||value.length>16){
                return '输入6~16个字符，区分大小写';
            }
        }
    });

    // 验证强度
    $("#password").keyup(function(){
         $("#pwdValid").css("display","flex");
         var val = $(this).val();
         var lvTxt = ['', '低', '中', '高'];
         var lv = 0;
         if (val.match(/[a-z]/g)) { lv++; } //验证是否包含字母
         if (val.match(/[0-9]/g)) { lv++; } // 验证是否包含数字
         if (val.match(/(.[^a-z0-9])/g)) { lv++; } //验证是否包含字母，数字，字符
         if (val.length < 6) { lv = 0; } //如果密码长度小于6位，提示消息为空
         if (lv > 3) { lv = 3; }

         $("#pwdValid").find("span").each(function(index){
            if(index < lv) {
                $(this).addClass("active");
            }else{
                $(this).removeClass("active");
            }
         });
         $("#pwdValid").find(".name").text(lvTxt[lv]);
    });

    // 发送验证码
    window.sendEmailCode = function (obj) {
        var email = $("#register-email").val();
        var reg = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
        if(email.match(reg)){
            obj.setAttribute("disabled", true);
            obj.value="重新发送(60)";
            // 倒计时
            var countdown = 59;
            var interval = setInterval(function() {
                if (countdown == 0) {
                    obj.removeAttribute("disabled");
                    obj.value="获取验证码";
                    countdown = 59;
                    clearInterval(interval);
                } else {
                    obj.setAttribute("disabled", true);
                    obj.value="重新发送(" + countdown + ")";
                    countdown--;
                }
            }, 1000);
            // 请求后台
            $.ajax({
                type: "GET",
                url: "register/email/code",
                data: {"email": email}
            });
        }else{
            $("#register-email").focus().addClass("layui-form-danger");
             layer.msg('邮箱格式不正确', {icon: 5, anim: 6});
        }
    };

    // 发送验证码
    window.sendLoginEmailCode = function (obj) {
        var email = $("#login-email").val();
        var reg = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
        if(email.match(reg)){
            obj.setAttribute("disabled", true);
            obj.value="重新发送(60)";
            // 倒计时
            var countdown = 59;
            var interval = setInterval(function() {
                if (countdown == 0) {
                    obj.removeAttribute("disabled");
                    obj.value="获取验证码";
                    countdown = 59;
                    clearInterval(interval);
                } else {
                    obj.setAttribute("disabled", true);
                    obj.value="重新发送(" + countdown + ")";
                    countdown--;
                }
            }, 1000);
            // 请求后台
            $.ajax({
                type: "GET",
                url: "login/email/code",
                data: {"email": email}
            });
        }else{
            $("#login-email").focus().addClass("layui-form-danger");
             layer.msg('邮箱格式不正确', {icon: 5, anim: 6});
        }
    };

    window.sendForgotEmailCode = function (obj) {
        var email = $("#forgot-email").val();
        var reg = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
        if(email.match(reg)){
            obj.setAttribute("disabled", true);
            obj.value="重新发送(60)";
            // 倒计时
            var countdown = 59;
            var interval = setInterval(function() {
                if (countdown == 0) {
                    obj.removeAttribute("disabled");
                    obj.value="获取验证码";
                    countdown = 59;
                    clearInterval(interval);
                } else {
                    obj.setAttribute("disabled", true);
                    obj.value="重新发送(" + countdown + ")";
                    countdown--;
                }
            }, 1000);
            // 请求后台
            $.ajax({
                type: "GET",
                url: "login/forgot/code",
                data: {"email": email}
            });
        }else{
            $("#forgot-email").focus().addClass("layui-form-danger");
             layer.msg('邮箱格式不正确', {icon: 5, anim: 6});
        }
    };
});
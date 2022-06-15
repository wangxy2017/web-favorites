layui.use('form', function(){
  var form = layui.form;

  //监听提交
  form.on('submit(formDemo)', function(data){
    $.ajax({
        type: "POST",
        url: "user/save",
        data: JSON.stringify(data.field),
        contentType: 'application/json;charset=utf-8',
        dataType: "json",
        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
        success: function (result) {
            if (result.code == 0) {
                window.location.href = "index.html";
            } else {
                layer.msg("保存失败", {icon: 5});
            }
        }
    });
    return false;
  });
});
layui.use(['form','layer'], function(){
    var form = layui.form;
    var layer = layui.layer;

  $.ajax({
      type: "GET",
      url: "system-config/info",
      dataType: "json",
      headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
      success: function (result) {
          if (result.code == 0) {
              var notice = result.data;
              form.val("notice-form", {
                  "NOTICE_TITLE": notice.NOTICE_TITLE
                  , "NOTICE_SHOW": notice.NOTICE_SHOW
              });
          }
      }
  });

  //监听提交
  form.on('submit(formDemo)', function(data){
      layer.load();
      $.ajax({
          type: "POST",
          url: "system-config/save",
          data: JSON.stringify(data.field),
          contentType: 'application/json;charset=utf-8',
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
});
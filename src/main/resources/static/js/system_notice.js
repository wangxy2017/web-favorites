layui.use(['form','layer'], function(){
    var form = layui.form;
    var layer = layui.layer;

    var E = window.wangEditor;
    var editor = new E('#NOTICE_CONTENT');
    editor.config.height = 300;
    editor.config.showFullScreen = false;
    editor.config.placeholder = '输入内容';
    editor.create();

  $.ajax({
      type: "GET",
      url: "system-notice/info",
      dataType: "json",
      headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
      success: function (result) {
          if (result.code == 0) {
              var notice = result.data;
              form.val("notice-form", {
                  "NOTICE_TITLE": notice.NOTICE_TITLE
                  , "NOTICE_SHOW": notice.NOTICE_SHOW
              });
              var text = notice.NOTICE_CONTENT;
              if(text){
                editor.txt.html(text);
              }
          }
      }
  });

  //监听提交
  form.on('submit(formDemo)', function(data){
      var html = editor.txt.html();
      var text = editor.txt.text();
      if(!html || !text){
        layer.msg("内容不能为空");
        return false;
      }
      if(text.length > 1000){
        layer.msg('最多输入1000个字符');
        return false;
      }
      data.field.NOTICE_CONTENT = html;
      layer.load();
      $.ajax({
          type: "POST",
          url: "system-notice/save",
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

  $("#clean").click(function(){
    layer.confirm('确认清除公告吗?', function(index){
      layer.close(index);
      $.ajax({
          type: "POST",
          url: "system-notice/save",
          data: JSON.stringify({"NOTICE_TITLE":"","NOTICE_CONTENT":"","NOTICE_SHOW":"2"}),
          contentType: 'application/json;charset=utf-8',
          dataType: "json",
          headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
          success: function (result) {
              if (result.code == 0) {
                   layer.msg("清除成功", {icon: 6});
                  $("#noticeForm")[0].reset();
                  editor.txt.html("");
              } else {
                  layer.msg(result.msg, {icon: 5});
              }
          }
      });
  });
  });
});
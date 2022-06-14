var E = window.wangEditor;
    var editor = new E('#content');
    editor.config.height = 300;
    editor.config.showFullScreen = false;
    editor.config.placeholder = '记录这一刻，晒给懂你的人'
    editor.create();

    // 编辑
    var url = window.location.href;
    if(url.lastIndexOf("#") > 0){
        var momentId = url.substring(url.lastIndexOf("#") + 1);
        $.ajax({
            type: "GET",
            url: "moment/" + momentId,
            dataType: "json",
            headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
            success: function (result) {
                if (result.code == 0 && result.data) {
                    editor.txt.html(result.data.content);
                }
            }
        });
    }
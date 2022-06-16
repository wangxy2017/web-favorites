layui.use(['layer','flow','util'], function() {
        var layer = layui.layer;
        var flow = layui.flow;
        var util = layui.util;

        // 加载数据
        window.loadList = function(){
            // 加载动画
            layer.load();
            $("#momentList").empty();
            var keyword = $("#search_text").val().trim();
            $('#center').unbind();
            flow.load({
                elem: '#momentList'
                ,scrollElem: '#center'
                ,mb: 400
                ,end: ' '
                ,done: function(page, next){
                  var lis = [];
                  $.ajax({
                        type: "GET",
                        url: "moment/list",
                        data: {"pageNum": page,"pageSize": 20,"text":keyword},
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            if(page == 1){
                                layer.closeAll('loading');
                                if(result.code == 0 && result.data.list.length > 0){
                                     $("#noData").hide();
                                }else{
                                     $("#noData").show();
                                }
                            }
                            if (result.code == 0) {
                                $.each(result.data.list, function(index, item){
                                    var html = '';
                                    html += '<div class="layui-timeline-item layui-anim layui-anim-fadein" data-id="' + item.id + '">';
                                    html += '   <i class="layui-icon layui-timeline-axis">&#xe63f;</i>';
                                    html += '   <div class="layui-timeline-content layui-text">';
                                    html += '       <p class="layui-timeline-title time">' + item.createTime + '</p>';
                                    html += '       <div class="moment">';
                                    html += '           <div class="content">'+ item.content +'</div>';
                                    html += '           <div class="content-mask">点击查看全文</div>';
                                    html += '           <div class="layui-anim layui-anim-fadein action" data-id="' + item.id + '">';
                                    html += '               <i class="layui-icon layui-icon-flag"></i>';
                                    html += '               <i class="layui-icon layui-icon-edit"></i>';
                                    html += '               <i class="layui-icon layui-icon-delete"></i>';
                                    html += '           </div>';
                                    html += '       </div>';
                                    html += '    </div>';
                                    html += '</div>';

                                    lis.push(html);
                                });
                                next(lis.join(''), page < result.data.pages);
                                hideBigText("#momentList");
                            }
                        }
                  });
                }
            });
        };

        loadList();

        // 固定块
        util.fixbar({
            top: true
            ,scrollElem: '#center'
            ,bgcolor: '#393D49'
            ,css: {right: 50, bottom: 80}
            ,mouseenter: function(type,ele){
                if(type === 'top'){
                    layer.tips('回到顶部', ele, {tips: 4});
                }
            }
            ,mouseleave: function(ele,type){
                layer.closeAll('tips');
            }
        });

        // 点击空白关闭
        $(document).on("click", function(e) {
            var _conss = $('.search-input');//点击的容器范围
            if (!_conss.is(e.target) && _conss.has(e.target).length === 0) {
                $("#search_close").hide();
                $("#search_text").parent().css({"width":"100px"});
            }
        });

        // 关闭搜索
        $("#search_close").click(function () {
            changeIndexMode();
            $(this).hide();
            $("#search_text").val("").parent().css({"width":"100px"});
        });

        window.searchCount = function(){
            // 记录搜索次数
            $.ajax({
                type: "GET",
                url: "user/search",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            });
        };

        $("#search_text").on("keydown", function(event){
            if(event.key === "Enter"){
                searchText();
            }
        }).on("focus", function () {
           $("#search_close").show();
           $(this).parent().css({"width":"200px"});
        });

        $("#search_icon").click(function(){
            searchText();
        });

        // 点击显示更多
        $(document).on('click', '.content-mask', function(e){
            var that = $(e.currentTarget);
            that.hide().prev().css("max-height", "none");
        });

        // 隐藏大文本
        window.hideBigText = function(containerId){
            $(containerId + " .content").each(function(){
                var height = parseInt($(this).height());
                var maxHeight = parseInt($(this).css("max-height"));
                if(maxHeight == 200 && height == 200){
                    $(this).next().show();
                }
            });
        };

        window.changeIndexMode = function(){
            $("#topMoment").show();
            loadList();
        };

        window.changeSearchMode = function(){
            $("#topMoment").hide();
            searchCount();
            loadList();
        };

        window.searchText = function(){
            var text = $("#search_text").val().trim();
            if (text == "") { //退出搜索模式
                $("#search_close").click();
            } else {
                changeSearchMode();
            }
        };

        // 编辑
        $(document).on('click', '.layui-icon-edit', function(e) {
            var id = $(e.currentTarget).parent().attr("data-id");
            $.ajax({
                type: "GET",
                url: "moment/" + id,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        var width = (windowWidth >= 800? 800 : windowWidth) + 'px';
                        layer.open({
                          id:"wangEditor",
                          type: 2,
                          content: 'moment_edit.html?' + timeSuffix() + '#' + id,
                          area: [width, '500px'],
                          btn: ['确认', '取消'],
                          yes: function(index, layero){
                            var frameId = $(layero).find("iframe").attr('id');
                            var subWindow = document.getElementById(frameId).contentWindow;
                            var html = subWindow.editor.txt.html();
                            var momentId = subWindow.momentId;
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
                                    url: "moment/update",
                                    data: JSON.stringify({"content": html,"id": momentId, "text": text}),
                                    contentType: 'application/json;charset=utf-8',
                                    dataType: "json",
                                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                                    success: function (result) {
                                        layer.closeAll('loading');
                                        if (result.code == 0) {
                                            layer.msg('保存成功', {icon: 6});
                                            layer.close(index);
                                            window.location.reload();
                                        } else {
                                            layer.msg('保存失败', {icon: 5});
                                        }
                                    }
                                });
                            }else{
                                layer.msg("内容不能为空");
                            }
                          }
                        });
                    }
                }
            });
        });

        initSearch("#search_text");

        // 置顶
        $(document).on('click', '.layui-icon-flag', function(e) {
            var id = $(e.currentTarget).parent().attr("data-id");
            $.ajax({
                type: "POST",
                url: "moment/top/" + id,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        layer.msg('操作成功', {icon: 6});
                        loadTopMoment();
                    }
                }
            });
        });

        // 删除
        $(document).on('click', '.layui-icon-delete', function(e) {
            var id = $(e.currentTarget).parent().attr("data-id");
            layer.confirm('确认删除吗?', function(index) {
                $.ajax({
                    type: "GET",
                    url: "moment/delete/" + id,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            window.location.reload();
                        }
                    }
                });
                layer.close(index);
            });
        });

        // 取消置顶
        $(document).on('click','.set-top',function(e){
            layer.confirm('确认取消置顶吗?', function(index) {
                var id = $(e.currentTarget).attr("data-id");
                $.ajax({
                    type: "DELETE",
                    url: "moment/top/" + id ,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            loadTopMoment();
                        }
                    }
                });
                layer.close(index);
            });
        });

        // 发布
        $("#addMoment").click(function() {
            var width = (windowWidth >= 800? 800 : windowWidth) + 'px';
            layer.open({
              id:"wangEditor",
              type: 2,
              title: "发布瞬间",
              content: 'moment_edit.html?' + timeSuffix(),
              area: [width,'500px'],
              btn: ['确认', '取消'],
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
                        url: "moment",
                        data: JSON.stringify({"content": html, "text": text}),
                        contentType: 'application/json;charset=utf-8',
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            layer.closeAll('loading');
                            if (result.code == 0) {
                                layer.msg('保存成功', {icon: 6});
                                layer.close(index);
                                window.location.reload();
                            } else {
                                layer.msg('保存失败', {icon: 5});
                            }
                        }
                    });
                }else{
                    layer.msg("内容不能为空");
                }
              }
            });
        });

        // 登出
        logout("#logout");

        window.loadTopMoment = function(){
            $("#topMoment").empty();
            $.ajax({
                type: "GET",
                url: "moment/top",
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0 && result.data) {
                        var moment = result.data;
                        var html = '';
                        html += '<div class="layui-timeline-item">';
                        html += '   <i class="layui-icon layui-timeline-axis" style="color: #ffb800;font-size: 22px;">&#xe658;</i>';
                        html += '   <div class="layui-timeline-content layui-text">';
                        html += '       <p class="layui-timeline-title time"><span class="layui-badge layui-bg-cyan set-top" data-id="' + moment.id + '">置顶</span>' + moment.createTime + '</p>';
                        html += '       <div class="moment">';
                        html += '           <div class="content">'+ moment.content +'</div>';
                        html += '           <div class="content-mask">点击查看全文</div>';
                        html += '       </div>';
                        html += '    </div>';
                        html += '</div>';
                        $("#topMoment").append(html);
                        hideBigText("#topMoment");
                    }
                }
            });
        };

        $(function(){
            loadTopMoment();
        });
    });
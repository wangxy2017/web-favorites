layui.use(['layer','flow','util','form'], function() {
        var layer = layui.layer;
        var flow = layui.flow;
        var util = layui.util;
        var form = layui.form;

        // 全局变量
        var searchIndex = -1;

        // 固定块
        util.fixbar({
            top: true
            ,bar1: '&#xe654;'
            ,bar2: '&#xe6dc;'
            ,scrollElem: '#center'
            ,bgcolor: '#393D49'
            ,css: {right: 50, bottom: 80}
            ,click: function(type,ele){
                if(type === 'bar1'){
                    addMemorandum();
                }else if(type === 'bar2'){
                    layer.tips('该功能暂未开放', ele, {tips: 4});
                }
            }
            ,mouseenter: function(type,ele){
                if(type === 'top'){
                    layer.tips('回到顶部', ele, {tips: 4});
                }else if(type === 'bar1'){
                    layer.tips('添加', ele, {tips: 4});
                }else if(type === 'bar2'){
                    layer.tips('录音', ele, {tips: 4});
                }
            }
            ,mouseleave: function(ele,type){
                layer.closeAll('tips');
            }
        });



        // 加载数据
        window.loadList = function(){
            // 加载动画
            layer.load();
            var keyword = $("#search_text").val().trim();
            $("#search_text").attr("data-lastSearch",keyword);
            $("#memorandumList").empty().next(".layui-flow-more").remove();
            $('#center').unbind();
            flow.load({
                elem: '#memorandumList'
                ,scrollElem: '#center'
                ,mb: 400
                ,end: ' '
                ,done: function(page, next){
                  var lis = [];
                  $.ajax({
                        type: "GET",
                        url: "memorandum/list",
                        data: {"pageNum": page,"pageSize": 20,"content":keyword},
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            if(page == 1){// 首次加载
                                layer.closeAll('loading');
                                if(keyword && result.code == 0 && result.data.list.length > 0){
                                    searchIndex = 0;
                                }else{
                                    searchIndex = -1;
                                    $("#search_info").hide();
                                }
                                if(result.code == 0 && result.data.list.length > 0){
                                     $("#notFoundDiv").hide();
                                }else{
                                     $("#notFoundDiv").show();
                                }
                            }
                            if (result.code == 0) {
                                $.each(result.data.list, function(index, item){
                                    var html = '';
                                    html += '<div class="memorandum-item layui-anim layui-anim-fadein" data-id="' + item.id + '">';
                                    if(keyword){
                                        html += '<div class="memorandum-content" data-keyword="'+ keyword +'" ondblclick="editContent(this,evnet)">'+ wrapSearch(escape(item.content).replace(/\n/g,'<br/>'),keyword) +'</div>';
                                    }else{
                                        html += '<div class="memorandum-content" ondblclick="editContent(this)">'+ escape(item.content).replace(/\n/g,'<br/>') +'</div>';
                                    }
                                    html += '</div>';
                                    lis.push(html);
                                });
                                next(lis.join(''), page < result.data.pages);
                            }
                        }
                  });
                }
            });
        };

        loadList();

        // 登出
        logout("#logout");

        // 点击空白关闭
        $(document).on("click", function(e) {
            var _conss = $('.search-input');//点击的容器范围
            if (!_conss.is(e.target) && _conss.has(e.target).length === 0) {
                $("#search_close").hide();
            }
        });





        window.preSearch = function(){
            var highLight = $(".highLight");
            var active = highLight.filter(".active");
            var index = highLight.index(active[0]);
            if(index > 0){
                active.removeClass("active");
                var pre = highLight.eq(index - 1).addClass("active");
                $("#search_info").find("span").text(index + "/" + highLight.length);
                $("#center").animate({scrollTop: pre[0].offsetTop - 100}, 50);
                searchIndex--;
            }
        };

        window.nextSearch = function(){
            var highLight = $(".highLight");
            var active = highLight.filter(".active");
            var index = highLight.index(active[0]);
            if(index < highLight.length - 1 ){
                active.removeClass("active");
                var next = highLight.eq(index + 1).addClass("active");
                $("#search_info").find("span").text((index + 2) + "/" + highLight.length);
                $("#center").animate({scrollTop: next[0].offsetTop - 100}, 50);
                searchIndex++;
            }
        };



        // 关闭搜索
        $("#search_close").click(function () {
            $(this).hide();
            $("#search_text").val("");
            $("#search_info").hide();
            searchIndex = -1;
            loadList();
        });

        initSearch("#search_text");

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
                var noChange = $(this).val() == $(this).attr("data-lastSearch");
                if(noChange && searchIndex >= 0){
                    var highLight = $(".highLight");
                    var active = highLight.eq(searchIndex);
                    if(active[0]){
                        $("#search_info").show().find("span").text((searchIndex + 1) + "/" + highLight.length);
                        $(".highLight.active").removeClass("active");
                        active.addClass("active");
                        $("#center").animate({scrollTop: active[0].offsetTop - 100}, 50);
                        if(searchIndex < highLight.length - 1){
                            searchIndex++;
                        }else{
                            searchIndex = 0;
                        }
                    }else{
                        $("#search_info").show().find("span").text("0/0");
                    }
                }else{
                    loadList();
                    if($(this).val() != ""){
                        searchCount();
                    }
                }
            } else if (event.key === "Backspace") {
                // 退出选择模式
                searchIndex = -1;
            }
        }).on("focus", function () {
           $("#search_close").show();
        });

        $("#search_icon").click(function(){
            loadList();
        });

        window.addMemorandum = function(){
            var width = (windowWidth > 800? 800 : windowWidth) + 'px';
            layer.prompt({
              formType: 2,
              placeholder: "请输入内容...",
              title: '添加备忘录',
              area: [width, '350px'] //自定义文本域宽高
            }, function(value, index, elem){
                if(value.trim() == ""){
                    layer.msg("请输入有效字符");
                    return false;
                }
                layer.close(index);
                layer.load();
                  $.ajax({
                    type: "POST",
                    url: "memorandum",
                    data: JSON.stringify({"content":value}),
                    contentType: 'application/json;charset=utf-8',
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        layer.closeAll('loading');
                        if (result.code == 0) {
                            window.location.reload();
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
            });
        };

        // 删除
        window.deleteMemorandum = function(id){
            layer.confirm('确认删除吗?', function(index) {
                $.ajax({
                    type: "GET",
                    url: "memorandum/delete/" + id,
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
        };

        // 编辑
        window.editContent = function(obj) {
            var id = $(obj).parent().attr("data-id");
            $.ajax({
                type: "GET",
                url: "memorandum/" + id,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        var width = (windowWidth > 800? 800 : windowWidth) + 'px';
                        layer.prompt({
                          formType: 2,
                          value: result.data.content,
                          placeholder: "请输入内容...",
                          title: '编辑备忘录',
                          area: [width, '350px'],
                          success: function(layero, index){
                            layero.prepend('<button class="layui-btn layui-btn-danger layui-btn-del" onclick="deleteMemorandum('+ id +')">删除</button>');
                          }
                        }, function(value, index, elem){
                            if(value.trim() == ""){
                                layer.msg("请输入有效字符");
                                return false;
                            }
                            layer.close(index);
                            layer.load();
                              $.ajax({
                                type: "POST",
                                url: "memorandum",
                                data: JSON.stringify({id: id, "content": value}),
                                contentType: 'application/json;charset=utf-8',
                                dataType: "json",
                                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                                success: function (result) {
                                    layer.closeAll('loading');
                                    if (result.code == 0) {
                                        window.location.reload();
                                    } else {
                                        layer.msg(result.msg, {icon: 5});
                                    }
                                }
                            });
                        });
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
        };
    });
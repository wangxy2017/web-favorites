layui.use(['layer','flow','util'], function() {
        var layer = layui.layer;
        var flow = layui.flow;
        var util = layui.util;

        // 加载动画
        layer.load();

        // 加载数据
        window.loadList = function(name){
            $("#favoritesList").empty();
            layer.load();
            $('#center').unbind();
            flow.load({
                elem: '#favoritesList'
                ,scrollElem: '#center'
                ,isLazyimg: true
                ,mb: 400
                ,end: ' '
                ,done: function(page, next){
                  var lis = [];
                  $.ajax({
                        type: "GET",
                        url: "share/list",
                        data: {"name": name, "pageNum": page,"pageSize": 100},
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            if(page == 1){
                                layer.closeAll('loading');
                                if (result.code == 0 && result.data.list.length > 0) {
                                    $("#notFoundDiv").hide();
                                }else{
                                    $("#notFoundDiv").show();
                                }
                            }
                            if (result.code == 0) {
                                $.each(result.data.list, function(index, item){
                                    var html = '';
                                    html += '<div class="favorites layui-anim layui-anim-fadein">';
                                    html += '   <div class="favorites-info">';
                                    html += '       <div class="bg">';
                                    html += '           <img src="images/book.svg" lay-src="' + item.icon + '">';
                                    html += '       </div>';
                                    html += '       <div class="title" lay-title="' + escape(item.name) + '" data-url="' + item.url + '" onclick="openUrl(this)">' + escape(item.name) + '</div>';
                                    html += '   </div>';
                                    html += '   <div class="other-info">';
                                    html += '       <div class="user" lay-title="'+ escape(item.nickName) +'"><i class="layui-icon layui-icon-username"></i><em>' + escape(item.nickName) + '</em></div>';
                                    html += '       <div class="support" data-id="' + item.id + '" data-support="' + item.support + '" onclick="support(this)"><i class="layui-icon layui-icon-star-fill"></i><em>' + transform(item.support) + '</em></div>';
                                    html += '   </div>';
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

        // 固定块
        util.fixbar({
            top: true
            ,scrollElem: '#center'
            ,bgcolor: '#393D49'
            ,css: {right: 50, bottom: 60}
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
            }
        });

        initSearch("#search_text");

        window.openUrl = function (obj) {
            var url = $(obj).attr("data-url");
            if(url.indexOf("https://") == 0 || url.indexOf("http://") == 0){
                newWin(url);
                // 记录访问次数
                $.ajax({
                    type: "GET",
                    url: "user/visit",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
                });
                // 记录点击率
                $.ajax({
                    type: "GET",
                    url: "share/click/" + id,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
                });
            }else{
                layer.msg('此链接无效', {icon: 7});
            }
        };



        window.support = function(obj){
            layer.confirm('确认收藏书签吗？', function(index){
                layer.close(index);

                var id = $(obj).attr("data-id");
                var support = $(obj).attr("data-support");
                if(support == null || isNaN(support)){
                    support = 0;
                }
                $.ajax({
                    type: "GET",
                    url: "share/support/" + id,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        layer.msg('收藏成功', {icon: 6});
                        if (result.code == 0) {
                            $(obj).find("em").text(transform(++support));
                        }
                    }
                });
            });
        };





        // 登出
        logout("#logout");

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
        });

        $("#search_icon").click(function(){
            searchText();
        });

        window.searchText = function(){
            var text = $("#search_text").val().trim();
            loadList(text);
            if(text != ""){
                searchCount();
            }
        };

        // 关闭搜索
        $("#search_close").click(function () {
            $(this).hide();
            $("#search_text").val("");
            loadList();
        });

    });
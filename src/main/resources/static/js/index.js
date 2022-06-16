//JavaScript代码区域
    layui.use(['element', 'layer', 'form', 'upload','flow','util'], function () {
        var element = layui.element;
        var layer = layui.layer;
        var form = layui.form;
        var upload = layui.upload;
        var flow = layui.flow;
        var util = layui.util;
        // 全局变量
        var favoritesLimit = 40;// 显示个数限制
        var navigationLimit = 6;// 显示个数限制
        var indexPageSize = 10;// 分页大小
        var currentPage = 1;// 当前页

        // 执行加载动画
        $("#loadingDiv").fadeIn();

        // 点击空白关闭
        $(document).on("click", function(e) {
            var _conss = $('.search-input');//点击的容器范围
            if (!_conss.is(e.target) && _conss.has(e.target).length === 0) {
                $(".search-items").hide();
                $("#search_close").hide();
            }
        });

        $('#layuiBody').unbind();
        flow.load({
            elem: '#categoryList'
            ,scrollElem: '#layuiBody'
            ,isLazyimg: true
            ,end: ' '
            ,mb: 400
            ,done: function(page, next){
              // 校正page值
              if(page > 1 && page <= currentPage){
                page = ++currentPage;
                next('',true,currentPage);
              }
              var lis = [];
              $.ajax({
                type: "GET",
                url: "favorites/list",
                data: {"pageNum":page, "pageSize":indexPageSize},
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if(page == 1){
                        $("#loadingDiv").fadeOut();
                        if(result.code == 0 && result.data.list.length > 0){
                             $("#notFoundDiv").hide();
                        }else{
                             $("#notFoundDiv").show();
                        }
                    }
                    if (result.code == 0) {
                        // 加载数据
                        $.each(result.data.list, function (i, c) {
                            var html = '';
                            var bookmarkClass = c.bookmark==1?"bookmark":"";
                            html += '<div class="category ' + bookmarkClass + '" data-id="' + c.id + '">';
                            html += '   <div class="title" onclick="editCategory(this,event)">' + c.name + '</div>';
                            html += '   <ul class="favorites">';
                            if(c.favorites.length > 0) {
                                $.each(c.favorites, function (j, f) {
                                    html += '   <li class="favorites-li layui-anim layui-anim-fadein" data-id="' + f.id + '" data-url="' + f.url + '" data-schema="' + f.schemaName + '" onclick="openUrl(this)">';
                                    html += '       <div class="favorites-bg">';
                                    html += '           <img src="images/book.svg" lay-src="' + f.icon + '"/>';
                                    html += '       </div>';
                                    html += '       <p  lay-title="' + f.name + '">' + f.name + '</p>';
                                    html += '       <i onclick="editFavorites(this,event)" class="action-icon layui-icon layui-icon-more-vertical"></i>';
                                    html += '   </li>';
                                    if(j == favoritesLimit - 1){
                                        html += '<li class="layui-anim layui-anim-fadein" onclick="moreFavorites(' + c.id + ')">';
                                        html += '   <div class="favorites-bg">';
                                        html += '       <img src="images/more.svg"/>';
                                        html += '   </div>';
                                        html += '   <p lay-title="显示更多">显示更多</p>';
                                        html += '</li>';
                                        return false;
                                    }
                                });
                            } else {
                                html += '<li class="layui-anim layui-anim-fadein" onclick="addFavorites(' + c.id + ')">';
                                html += '   <div class="favorites-bg">';
                                html += '       <img src="images/add.svg"/>';
                                html += '   </div>';
                                html += '   <p lay-title="添加网址">添加网址</p>';
                                html += '</li>';
                            }
                            html += '   </ul>';
                            html += '</div>';
                            lis.push(html);
                        });
                        next(lis.join(''), page < result.data.pages);
                    }
                },
                error: function(){
                    layer.msg("服务器异常", {icon: 2});
                }
              });
            }
        });

        // 固定块
        util.fixbar({
            top: true
            ,bar1: '&#xe615;'// 搜索
            ,bar2: '&#xe60a;'// 备忘录
            ,bar3: '&#xe68d;'// 瞬间
            ,bar4: '&#xe637;'// 日历
            ,bar5: '&#xe681;'// 文件
            ,scrollElem: '#layuiBody'
            ,bgcolor: '#393D49'
            ,css: {right: windowWidth < 800 ? 30 : 50, bottom: windowWidth < 800 ? 54 : 80}
            ,click: function(type){
                if(type === 'bar1'){
                    window.location.href = "search.html";
                }else if(type === 'bar2'){
                    window.location.href = "memorandum.html";
                }else if(type === 'bar3'){
                    window.location.href = "moment.html";
                }else if(type === 'bar4'){
                    window.location.href = "calendar.html";
                }else if(type === 'bar5'){
                    window.location.href = "file.html";
                }
            }
            ,mouseenter: function(type,ele){
                if(type === 'top'){
                    layer.tips('回到顶部', ele, {tips: 4});
                }else if(type === 'bar1'){
                    layer.tips('搜索', ele, {tips: 4});
                }else if(type === 'bar2'){
                    layer.tips('备忘录', ele, {tips: 4});
                }else if(type === 'bar3'){
                    layer.tips('瞬间', ele, {tips: 4});
                }else if(type === 'bar4'){
                    layer.tips('日程', ele, {tips: 4});
                }else if(type === 'bar5'){
                    layer.tips('文件', ele, {tips: 4});
                }
            }
            ,mouseleave: function(ele,type){
                layer.closeAll('tips');
            }
        });

        //执行实例
        var uploadInst = upload.render({
            elem: '#import' //绑定元素
            , accept: 'file'
            , exts: 'xml'
            , url: 'favorites/import' //上传接口
            , headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            , before: function(obj){
                layer.close(indexMap.get('#importOrExport')); //关闭弹层
                layer.load(); //上传loading
            }
            , done: function (result) {
                layer.closeAll('loading'); //关闭loading
                if (result.code == 0) {
                    window.location.reload();//刷新
                } else {
                    layer.msg(result.msg, {icon: 5});
                }
            }
            , error: function () {
                layer.closeAll('loading'); //关闭loading
                layer.msg('导入失败', {icon: 5});
            }
        });

        //执行实例
        var uploadInst = upload.render({
            elem: '#importLocal' //绑定元素
            , accept: 'file'
            , exts: 'html'
            , url: 'favorites/importHtml' //上传接口
            , headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            , before: function(obj){
                layer.close(indexMap.get('#importOrExport')); //关闭弹层
                layer.load(); //上传loading
            }
            , done: function (result) {
                layer.closeAll('loading'); //关闭loading
                if (result.code == 0) {
                    window.location.reload();//刷新
                } else {
                    layer.msg(result.msg, {icon: 5});
                }
            }
            , error: function () {
                layer.closeAll('loading'); //关闭loading
                layer.msg('导入失败', {icon: 5});
            }
        });

        $("#shortcutMike").click(function(){
            layer.tips('该功能暂未开放', this, {tips: 3});
        });

        $("#searchMike").click(function(){
            layer.tips('该功能暂未开放', this, {tips: 3});
        });

        // 清除密码
        $("#unsetPwd").click(function(){
            var passwordId = $("#pwdId").val();
            if(passwordId == ""){
                layer.msg('未设置密码');
                return false;
            }
            layer.confirm('确认清除吗?', function(index){
                layer.close(index);

                layer.load();
                $.ajax({
                    type: "DELETE",
                    url: "password/" + passwordId,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        layer.closeAll('loading');
                        if (result.code == 0) {
                            layer.msg('操作成功', {icon: 6});
                            // 重置表单
                            form.val("setting-pwd", {
                                "id": ""
                                , "account": ""
                                , "password": ""
                            });
                        } else {
                           layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
            });
        });

        $("#catalog_search_name").on('input',function(){
            $("#catalog_search_close").show();
            var name = $(this).val();
            $("#catalogList").children("li").each(function(i,item){
                if($(item).text().indexOf(name) != -1){
                    $(item).show();
                }else{
                    $(item).hide();
                }
            });
        });

        // 关闭搜索
        $("#catalog_search_close").click(function () {
            $("#catalog_search_name").val("").trigger("input");
            $(this).hide();
        });

        $("#edit_url_input").on('input',function(){
            var url = $(this).val();
            if(url.indexOf("https://") == 0 || url.indexOf("http://") == 0){
                $("#edit_name_input").addClass("layui-disabled").attr("disabled",true);
                $("#edit_url_loading").show();
                // 请求后台解析url
                $.ajax({
                    type: "GET",
                    url: "favorites/url",
                    data: {"url": url},
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        $("#edit_name_input").removeClass("layui-disabled").removeAttr("disabled",true);
                        $("#edit_url_loading").hide();

                        if (result.code == 0 && result.data != "") {
                            $("#edit_name_input").val(result.data);
                        }
                    },
                    error: function(){
                        $("#edit_name_input").removeClass("layui-disabled").removeAttr("disabled",true);
                        $("#edit_url_loading").hide();
                    }
                });
            }
        });

        $("#url_input").on('input',function(){
            var url = $(this).val();
            if(url.indexOf("https://") == 0 || url.indexOf("http://") == 0){
                $("#name_input").addClass("layui-disabled").attr("disabled",true);
                $("#url_loading").show();
                // 请求后台解析url
                $.ajax({
                    type: "GET",
                    url: "favorites/url",
                    data: {"url": url},
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        $("#name_input").removeClass("layui-disabled").removeAttr("disabled",true);
                        $("#url_loading").hide();

                        if (result.code == 0 && result.data != "") {
                            $("#name_input").val(result.data);
                        }
                    },
                    error: function(){
                        $("#name_input").removeClass("layui-disabled").removeAttr("disabled",true);
                        $("#url_loading").hide();
                    }
                });
            }
        });

        $("#nav_url_input").on('input',function(){
            var url = $(this).val();
            if(url.indexOf("https://") == 0 || url.indexOf("http://") == 0){
                $("#nav_name_input").addClass("layui-disabled").attr("disabled",true);
                $("#nav_url_loading").show();
                // 请求后台解析url
                $.ajax({
                    type: "GET",
                    url: "favorites/url",
                    data: {"url": url},
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        $("#nav_name_input").removeClass("layui-disabled").removeAttr("disabled",true);
                        $("#nav_url_loading").hide();

                        if (result.code == 0 && result.data != "") {
                            $("#nav_name_input").val(result.data);
                        }
                    },
                    error: function(){
                        $("#nav_name_input").removeClass("layui-disabled").removeAttr("disabled",true);
                        $("#nav_url_loading").hide();
                    }
                });
            }
        });

        window.isMobile = function(){
            let info = navigator.userAgent;
            let agents = ["Android", "iPhone", "SymbianOS", "Windows Phone", "iPod", "iPad"];
            for(let i = 0; i < agents.length; i++){
                if(info.indexOf(agents[i]) >= 0) return true;
            }
            return false;
        };

        $("#settingPwd").click(function () {
            layer.open({
                type: 1,
                title: "管理登录此网站的密码",
                content: $('#setting-pwd')
            });
            // 表单赋值
            $.ajax({
                type: "GET",
                url: "password/fid/" + $("#favoritesId").val(),
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        let data = result.data;
                        //给表单赋值
                        form.val("setting-pwd", {
                            "id": data.id
                            , "favoritesId": data.favoritesId
                            , "account": data.account
                            , "password": data.password
                        });
                    } else {
                        form.val("setting-pwd", {
                            "id": ""
                            , "favoritesId": $("#favoritesId").val()
                            , "account": ""
                            , "password": ""
                        });
                    }
                }
            });
        });

        $("#export").click(function () {
            var favorites = $("#favorites").is(":checked")?"1":"0";
            var moment = $("#moment").is(":checked")?"1":"0";
            var task = $("#task").is(":checked")?"1":"0";
            var search = $("#search").is(":checked")?"1":"0";
            var navigation = $("#navigation").is(":checked")?"1":"0";
            var memorandum = $("#memorandum").is(":checked")?"1":"0";
            layer.close(indexMap.get('#importOrExport'));
            var url = "favorites/export?favorites=" + favorites + "&moment=" + moment + "&task=" + task + "&search=" + search + "&navigation=" + navigation + "&memorandum=" + memorandum;
            downloadFile('export.xml', url);
        });

        $("#cancelPwd").click(function () {
            layer.close(indexMap.get('#changePwd'));
        });

        $("#cancelEmail").click(function () {
            layer.close(indexMap.get('#changeEmail'));
        });

        $("#cancelAddFavorites").click(function () {
            layer.close(indexMap.get('#add-favorites'));
        });

        $("#cancelAddFastNav").click(function () {
            layer.close(indexMap.get('#add-fastNav'));
        });

        window.downloadFile = function(filename, url){
            $.ajax({
                url: url,
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                xhrFields: { responseType: "arraybuffer" },
                success: function(result){
                    var a = document.createElement('a');
                    a.download = filename;
                    a.style.display = 'none';
                    var blob = new Blob([result]);
                    a.href = URL.createObjectURL(blob);
                    document.body.appendChild(a);
                    a.click();
                    document.body.removeChild(a);
                },
                error: function(){
                    layer.msg('下载失败', {icon: 5});
                }
            });
        };

        // 搜索项绑定点击事件
        $(document).on('click', '.search-items li', function() {
            var text = $(this).text();
            $("#search_name").val(text).focus();
            $(this).parent().hide();
            searchFavorites(text);
        });

        $(document).on('click', '.search-items li .history-close', function(event) {
            layui.stope(event);

            $(this).parent().remove();
            if($(".search-items").children().length === 0) $(".search-items").hide();

            var text = $(this).parent().text();
            var input_history = JSON.parse(localStorage.getItem("input_history"));
            if(input_history.indexOf(text) >= 0) {
                input_history.remove(text);
                localStorage.setItem("input_history", JSON.stringify(input_history));
            }
        });

        $("#importOrExportBtn").click(function () {
            var index = layer.open({
                type: 1,
                area: windowWidth < 800 ? '360px' : '400px',
                title: false,
                closeBtn: 0,
                content: $('#importOrExport')
            });
            indexMap.set('#importOrExport',index);
        });

        // 添加收藏
        $("#addFavoritesBtn").click(function () {
            var index = layer.open({
                type: 1,
                title: "添加收藏",
                skin: 'to-fix-select',
                content: $('#add-favorites')
            });
            indexMap.set('#add-favorites',index);
            // 表单赋值
            form.val("add-favorites", {
                "name": ""
                , "url": ""
            });
        });

        // 添加快捷导航
        window.addFastNav = function(){
            var index = layer.open({
                type: 1,
                title: "添加快捷导航",
                content: $('#add-fastNav')
            });
            indexMap.set('#add-fastNav',index);
            // 表单赋值
            form.val("add-fastNav", {
                "name": ""
                , "url": ""
            });
        };

        // 图片懒加载
        window.loadImg =  function loadImg(ele) {
            $(ele).find("img[lay-src]").each(function(i,item){
                var originUrl = item.getAttribute("src");
                var url = item.getAttribute("lay-src");
                var img = new Image();
                img.src = url;
                if(img.complete){
                  item.setAttribute('src', url);
                }
                img.onload = function(){
                  img.onload = null;
                  item.setAttribute('src', url);
                };
                img.onerror = function(e){
                  img.onerror = null;
                  item.setAttribute('src', originUrl);
                };
            });
        };

        // 快捷导航
        $("#fastNavBtn").click(function () {
            $.ajax({
                type: "GET",
                url: "quick-navigation/list",
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        // 加载数据
                        var html = '   <ul class="favorites" id="dragMenu">';
                        if(result.data.length > 0) {
                            $.each(result.data, function (j, f) {
                                html += '   <li class="favorites-li layui-anim layui-anim-fadein" data-id="' + f.id + '" data-url="' + f.url + '" data-schema="' + f.schemaName + '" onclick="openNav(this)">';
                                html += '       <div class="favorites-bg">';
                                html += '           <img src="images/book.svg" lay-src="' + f.icon + '"/>';
                                html += '       </div>';
                                html += '       <p  lay-title="' + f.name + '">' + f.name + '</p>';
                                html += '       <i onclick="deleteFastNav(this,event)" class="deleteFastNav action-icon layui-icon layui-icon-delete"></i>';
                                html += '   </li>';
                            });
                        }
                        var hideCss = result.data.length < navigationLimit ? '' : 'style="display:none"';
                        html += '       <li id="addFastNavBtn" class="layui-anim layui-anim-fadein" onclick="addFastNav()" '+ hideCss +'>';
                        html += '           <div class="favorites-bg">';
                        html += '               <img src="images/add.svg"/>';
                        html += '           </div>';
                        html += '           <p lay-title="添加网址">添加网址</p>';
                        html += '       </li>';
                        html += '   </ul>';
                        // 弹出层
                        var width = (windowWidth > 800 ? 800 : (windowWidth < 360 ? 360 : windowWidth)) + 'px';
                        layer.open({
                            id: "fastNav",
                            type: 1,
                            title: "快捷导航",
                            area: width,
                            content: '<div>' + html + '</div>',
                            success: function(layero, index){
                                loadImg(layero);
                            }
                        });
                        // 初始化拖拽插件
                        var container = document.getElementById("dragMenu");
                        var sort = Sortable.create(container, {
                          handle: ".favorites-bg",
                          draggable: ".favorites-li",
                          onEnd: function (evt){
                             var arr = [];
                             $("#dragMenu .favorites-li").each(function(i,item){
                                var nav = {};
                                nav.id = $(item).attr("data-id");
                                nav.sort = i;
                                arr[i] = nav;
                             });
                             $.ajax({
                                type: "POST",
                                url: "quick-navigation/sort",
                                data: JSON.stringify(arr),
                                contentType: 'application/json;charset=utf-8',
                                dataType: "json",
                                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                                success: function (result) {
                                    if (result.code != 0) {
                                        layer.msg("操作失败", {icon: 5});
                                    }
                                }
                            });
                          }
                        });
                    }
                },
                error: function(){
                    layer.msg("服务器异常", {icon: 2});
                }
              });
        });

        // 关闭搜索
        $("#search_close").click(function () {
            $(this).hide();
            $("#search_name").val("");
            $(".search-items").hide();
            changeIndexMode();
        });

        // 搜索指令
        window.searchShortcut = function(name){
            layer.load();
            $.ajax({
                type: "GET",
                url: "favorites/shortcut?key=" + name,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    $("#search_name").val("");
                    $("#search_close").hide();
                    if (result.code == 0) {
                        $(".favorites-li[data-id=" + result.data.id + "]").click();
                    }else{
                        layer.tips('未找到指令', '#search_name', {tips: 1});
                    }
                }
            });
        };

        // 切换搜索模式
        window.changeSearchMode = function(){
            $("#main").hide();
            $("#positionDiv").hide();
            $("#starDiv").hide();
            $("#notFoundDiv").hide();
            $("#searchDiv").show();
        };

        // 切换搜索模式
        window.changePositionMode = function(){
            $("#starDiv").hide();
            $("#main").hide();
            $("#notFoundDiv").hide();
            $("#searchDiv").hide();
            $("#positionDiv").show();
        };

        // 切换首页模式
        window.changeIndexMode = function(){
            $("#notFoundDiv").hide();
            $("#searchDiv").hide();
            $("#positionDiv").hide();
            $("#starDiv").show();
            $("#main").show();
        };


        // 搜索文本
        window.searchFavorites = function(name){
            changeSearchMode();
            layer.load();
            $.ajax({
                type: "GET",
                url: "favorites/search",
                data: {"name": name},
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    $("#searchDiv").empty();
                    if (result.code == 0 && (result.data.favoritesList.length > 0 || result.data.categoryList.length > 0)) {
                        var html = '';
                        html += '<div class="positionNav">';
                        html += '   <span class="layui-breadcrumb" lay-separator="/" lay-filter="search-nav">';
                        html += '       <a href="javascript:window.location.reload();">首页</a>';
                        html += '       <a><cite>搜索</cite></a>';
                        html += '   </span>';
                        html += '</div>';
                        html += '<ul class="favorites">';
                        $.each(result.data.categoryList, function (i, c) {
                            html += '<li class="favorites-li layui-anim layui-anim-fadein" data-id="' + c.id + '" data-name="' + c.name + '" onclick="showFavorites(this)">';
                            html += '   <div class="favorites-bg">';
                            html += '       <img src="images/category.svg"/>';
                            html += '   </div>';
                            html += '   <p  lay-title="' + c.name + '">' + c.name + '</p>';
                            html += '   <i onclick="editCategory(this,event)" class="action-icon layui-icon layui-icon-more-vertical"></i>';
                            html += '</li>';
                        });
                        $.each(result.data.favoritesList, function (i, f) {
                            html += '<li class="favorites-li layui-anim layui-anim-fadein" data-id="' + f.id + '" data-url="' + f.url + '" data-schema="' + f.schemaName + '" onclick="openUrl(this)">';
                            html += '   <div class="favorites-bg">';
                            html += '       <img src="images/book.svg" lay-src="' + f.icon + '"/>';
                            html += '   </div>';
                            html += '   <p  lay-title="' + f.name + '">' + f.name + '</p>';
                            html += '   <i onclick="editFavorites(this,event)" class="action-icon layui-icon layui-icon-more-vertical"></i>';
                            html += '</li>';
                        });
                        html += '</ul>';
                        var searchDiv = $("#searchDiv").append(html);
                        loadImg(searchDiv);
                        element.render('breadcrumb', 'search-nav');
                        // 存入历史记录
                        var input_history = JSON.parse(localStorage.getItem("input_history"));
                        if (input_history == null || input_history.length >= 5) input_history = [];
                        if (input_history.indexOf(name) < 0) input_history.push(name);
                        localStorage.setItem("input_history", JSON.stringify(input_history));
                    }else{
                        $("#notFoundDiv").show();
                    }
                },
                error: function(){
                    layer.closeAll('loading');
                    layer.msg('服务器异常', {icon: 2});
                }
            });
        };

        window.showFavorites = function(obj){
            layer.load();
            $.ajax({
                type: "GET",
                url: "category/favorites/" + $(obj).attr("data-id"),
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    $("#searchDiv").empty();
                    if (result.code == 0 && result.data.favorites.length > 0) {
                        var c = result.data;
                        var html = '';
                        var bookmarkClass = c.bookmark==1?"bookmark":"";
                        html += '<div class="positionNav">';
                        html += '   <span class="layui-breadcrumb" lay-separator="/" lay-filter="category-nav">';
                        html += '       <a href="javascript:window.location.reload();">首页</a>';
                        html += '       <a><cite>' + c.name + '</cite></a>';
                        html += '   </span>';
                        html += '</div>';
                        html += '<ul class="favorites ' + bookmarkClass + '">';
                        $.each(c.favorites, function (i, f) {
                            html += '<li class="favorites-li layui-anim layui-anim-fadein" data-id="' + f.id + '" data-url="' + f.url + '" data-schema="' + f.schemaName + '" onclick="openUrl(this)">';
                            html += '    <div class="favorites-bg">';
                            html += '        <img src="images/book.svg" lay-src="' + f.icon + '"/>';
                            html += '    </div>';
                            html += '    <p  lay-title="' + f.name + '">' + f.name + '</p>';
                            html += '    <i onclick="editFavorites(this,event)" class="action-icon layui-icon layui-icon-more-vertical"></i>';
                            html += '</li>';
                        });
                        html += '</ul>';
                        var searchDiv = $("#searchDiv").append(html).show();
                        loadImg(searchDiv);
                        element.render('breadcrumb', 'category-nav');
                        $("#notFoundDiv").hide();
                    }else{
                        $("#notFoundDiv").show();
                    }
                },
                error: function(){
                    layer.closeAll('loading');
                    layer.msg('服务器异常', {icon: 2});
                }
            });
        };

        initSearch("#search_name");

        window.searchCount = function(){
            // 记录搜索次数
            $.ajax({
                type: "GET",
                url: "user/search",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            });
        };

        // 搜索
        $("#search_name").on("keydown", function (event) {
            // 1.按上下键进入选择模式，并控制，返回键退出选择模式
            // 2.在选择模式时，按下回车跳转超链接，输入值，退出选择模式
            // 3.不在选择模式时，输入框有值，按下回车查询，输入框没有值，按下回车退出查询模式
            if (event.key === "Enter") {
                var hidden = $(".search-items").is(":hidden");
                var activeLi = $(".favorites-li.active:visible")[0];
                if(typeof activeLi !== "undefined" && hidden){
                    $(activeLi).click();
                }else{
                    var name = $(this).val().trim();
                    if (name == "") { //退出搜索模式
                        $("#search_close").click();
                    } else if(name.indexOf("打开") == 0 || name.indexOf("Open") == 0) { //搜索快捷指令
                        searchShortcut(name);
                        searchCount();
                    } else {// 搜索文本
                        searchFavorites(name);
                        searchCount();
                    }
                }
                if(!hidden){
                    $(".search-items").hide();
                }
            } else if (event.key === "ArrowDown") {
                // 判断搜索选项是否隐藏，如果隐藏，则进入选择模式
                var hidden = $(".search-items").is(":hidden");
                if(hidden){
                    var activeLi = $(".favorites-li.active:visible")[0];
                    var list = $(".favorites-li:visible");
                    if(typeof activeLi === "undefined"){
                        list.eq(0).addClass("active");
                    }else{
                        var nextActiveLi;
                        var last;
                        list.each(function(index,item){
                            if($(item).hasClass("active")){
                                var nextIndex = index + 1;
                                nextActiveLi = list[nextIndex];
                                if(nextIndex + 1 === list.length){
                                    last = true
                                }
                                return false;
                            }
                        });
                        if(typeof nextActiveLi !== "undefined"){
                            $(activeLi).removeClass("active");
                            $(nextActiveLi).addClass("active");
                            // 滚动
                            if(last){
                                $("#layuiBody").animate({scrollTop: $("#layuiBody")[0].scrollHeight}, 50);
                            }else{
                                elasticScroll(nextActiveLi.offsetTop);
                            }
                        }
                    }
                }else{
                    var hover_in = typeof $(".search-items li.hover-in")[0] === "undefined" ? $(".search-items li:first")[0] : $(
						".search-items li.hover-in").next()[0];
					if (typeof hover_in !== "undefined") {
						$(hover_in).addClass('hover-in').siblings().removeClass('hover-in');
						$(this).val(hover_in.innerText);
					}else{
					    $(".search-items").hide();
					}
                }
            } else if (event.key === "ArrowUp") {
                // 判断搜索选项是否隐藏，如果隐藏，则进入选择模式
                var hidden = $(".search-items").is(":hidden");
                if(hidden){
                    var activeLi = $(".favorites-li.active:visible")[0];
                    var list = $(".favorites-li:visible");
                    if(typeof activeLi === "undefined"){
                        list.eq(0).addClass("active");
                    }else{
                        var nextActiveLi;
                        var first;
                        list.each(function(index,item){
                            if($(item).hasClass("active")){
                                var nextIndex = index - 1;
                                nextActiveLi = list[nextIndex];
                                if(nextIndex === 0){
                                    first = true;
                                }
                                return false;
                            }
                        });
                        if(typeof nextActiveLi !== "undefined"){
                            $(activeLi).removeClass("active");
                            $(nextActiveLi).addClass("active");
                            // 滚动
                            if(first){
                                $("#layuiBody").animate({scrollTop: 0}, 50);
                            }else{
                                elasticScroll(nextActiveLi.offsetTop);
                            }
                        }
                    }
                }else{
                    var hover_in = typeof $(".search-items li.hover-in")[0] === "undefined" ? $(".search-items li:last")[0] : $(
						".search-items li.hover-in").prev()[0];
					if (typeof hover_in !== "undefined") {
						$(hover_in).addClass('hover-in').siblings().removeClass('hover-in');
						$(this).val(hover_in.innerText);
					}else{
					    $(".search-items").hide();
					}
                }
            } else if (event.key === "Backspace") {
                // 退出选择模式
                $(".favorites-li.active").removeClass("active");
            }
        }).on("input", function () {
            //退出选择模式
            $(".favorites-li.active").removeClass("active");
        }).on("focus", function () {
           $("#search_close").show();
            var html = '';
            var input_history = JSON.parse(localStorage.getItem("input_history"));
            $.each(input_history, function(index, item) {
                html = '<li>' + item + '<i class="layui-icon layui-icon-close history-close"></i></li>' + html;
            });
            if (html !== "") $(".search-items").empty().append(html).show();
        });
        // 添加收藏
        window.addFavorites = function (categoryId) {
            $("#addFavoritesBtn").click();
            form.val("add-favorites", {
                "categoryId": categoryId
            });
        }
        // 发送验证码
        window.sendEmailCode = function (obj) {
            var email = $("#newEmail").val();
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
                $("#newEmail").focus().addClass("layui-form-danger");
                layer.msg('邮箱格式不正确', {icon: 5, anim: 6});
            }
        };

        // 弹性滚动
        window.elasticScroll = function(height){
            var visibleHeight = $("#layuiBody").height();
            var shouldScroll = Math.floor(height/ visibleHeight) * visibleHeight;
            var scrollTop = $("#layuiBody").scrollTop();
            if(Math.abs(scrollTop - shouldScroll) >= visibleHeight){
                $("#layuiBody").animate({scrollTop: shouldScroll}, 50);
            }
        };

        window.isEmpty = function(obj){
            if(typeof obj == "undefined" || obj == "undefined" || obj == null || obj == "" || obj == "none" || obj == "null"){
                return true;
            }else{
                return false;
            }
        };

        // 打开新窗口
        window.openUrl = function (obj) {
            var url = $(obj).attr("data-url");
            var schema = $(obj).attr("data-schema");
            if(url.indexOf("https://") == 0 || url.indexOf("http://") == 0){
                if(!isEmpty(schema)&&isMobile()){
                    layer.confirm('是否打开app？', {
                      btn: ['确认', '取消']
                    }, function(index, layero){
                        url = schema + url.substring(url.indexOf("://"));
                        newWin(url);
                        layer.close(index);
                    }, function(index){
                        newWin(url);
                        layer.close(index);
                    });
                }else{
                    newWin(url);
                }
                // 记录访问时间
                $.ajax({
                    type: "GET",
                    url: "favorites/visit/" + $(obj).attr("data-id"),
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
                });
            }else{
                layer.msg('此链接无效', {icon: 7});
            }
        };

        window.openNav = function (obj) {
            var url = $(obj).attr("data-url");
            if(url.indexOf("https://") == 0 || url.indexOf("http://") == 0){
                newWin(url);
                // 记录访问次数
                $.ajax({
                    type: "GET",
                    url: "user/visit",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
                });
            }else{
                layer.msg('此链接无效', {icon: 7});
            }
        };

        window.newWin = function(url) {
          var a = document.createElement('a');
          a.setAttribute('href', url);
          a.setAttribute('target', '_blank');
          document.body.appendChild(a);
          a.click();
          a.remove();
        };

        // 编辑分类
        window.editCategory = function (obj, e) {
            layui.stope(e);
            var index = layer.open({
                type: 1,
                title: "修改分类",
                content: $('#update-category')
            });
            indexMap.set('#update-category',index);
            // 表单赋值
            $.ajax({
                type: "GET",
                url: "category/" + $(obj).parent().attr("data-id"),
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        let data = result.data;
                        //给表单赋值
                        form.val("update-category", {
                            "id": data.id
                            , "name": data.name
                            , "sort": data.sort
                            , "bookmark": data.bookmark
                        });
                        //禁用系统分类
                        if (data.isSystem == 1) {
                            $("#categoryName").attr("disabled", true).addClass("layui-disabled");
                            $("#categorySort").attr("disabled", true).addClass("layui-disabled");
                            $("#deleteCategory").attr("disabled", true).addClass("layui-btn-disabled");
                        } else {
                            $("#categoryName").removeAttr("disabled").removeClass("layui-disabled");
                            $("#categorySort").removeAttr("disabled").removeClass("layui-disabled");
                            $("#deleteCategory").removeAttr("disabled").removeClass("layui-btn-disabled");
                        }
                    }
                }
            });
        };

        // 新增分类
        $("#addCategoryBtn").click(function () {
            layer.prompt({title: "添加分类", placeholder:"输入分类名称", maxlength: 20}, function (value, index, elem) {
                // 验证
                var bool = true;
                $.ajax({
                    type: "GET",
                    url: 'category/check/' + escapeHtml(value),
                    async: false,
                    dataType: 'json',
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            layer.tips('分类已存在', elem, {tips: 3});
                            bool = false;
                        }
                    }
                });
                if(bool){
                    layer.close(index);
                    layer.load();
                    $.ajax({
                        type: "POST",
                        url: "category",
                        data: JSON.stringify({"name": value}),
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
                }
            });
        });

        // 新增分类
        $("#addCategoryIcon").click(function () {
            layer.prompt({title: "添加分类", placeholder:"输入分类名称", maxlength: 20}, function (value, index, elem) {
                // 验证
                var bool = true;
                $.ajax({
                    type: "GET",
                    url: 'category/check/' + escapeHtml(value),
                    async: false,
                    dataType: 'json',
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            layer.tips('分类已存在', elem, {tips: 3});
                            bool = false;
                        }
                    }
                });
                if(bool){
                    layer.close(index);
                    layer.load();
                    $.ajax({
                        type: "POST",
                        url: "category",
                        data: JSON.stringify({"name": value}),
                        contentType: 'application/json;charset=utf-8',
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            layer.closeAll('loading');
                            if (result.code == 0) {
                                loadCategoryList(result.data.id);
                            } else {
                                layer.msg(result.msg, {icon: 5});
                            }
                        }
                    });
                }
            });
        });

        window.exportNow = function(){
            $("#export").click();
            layer.closeAll();
        };

        // 清除数据
        $("#cleanData").click(function(){
            var aHtml = '<a class="layui-blue" href="javascript:exportNow();">立即备份</a>';
            layer.confirm('该操作会清除您在平台上的所有数据，且不可恢复，清除数据前应做好备份('+ aHtml +')，谨慎操作！确认清除所有数据吗？', {title:'清除数据'}, function(index){
                layer.close(index);

                layer.prompt({formType: 1, title: "登录密码", placeholder:"请输入登录密码", maxlength: 20}, function(value, index, elem){
                    $.ajax({
                        type: "POST",
                        url: "user/cleanData",
                        data: {"loginPwd":md5(value)},
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            if (result.code == 0) {
                                window.location.reload();
                            } else {
                                layer.msg(result.msg, {icon: 5});
                            }
                        }
                    });
                  layer.close(index);
                });
            });
        });

        // 修改密码
        $("#changePwd").click(function () {
            var index = layer.open({
                type: 1,
                title: "修改密码",
                content: $('#passwordForm')
            });
            indexMap.set('#changePwd',index);
            // 表单赋值
            form.val("passwordForm", {
                "oldPassword": ""
                , "newPassword": ""
                , "confirmPassword": ""
            });
        });

        // 修改邮箱
        $("#changeEmail").click(function () {
            var index = layer.open({
                type: 1,
                title: "修改邮箱",
                content: $('#emailForm')
            });
            indexMap.set('#changeEmail',index);
            // 表单赋值
            form.val("emailForm", {
                "newEmail": ""
                , "code": ""
            });
        });

        // 登出
        logout("#logout");

        // 加载分类
        window.loadCategoryList = function (id) {
            $.ajax({
                type: "GET",
                url: "category/list",
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        var oHtml = '';
                        var lHtml = '';
                        $.each(result.data, function (i, c) {
                            c.page = parseInt(i / indexPageSize) + 1;
                            oHtml += '<option value="' + c.id + '" ' + ((c.id == id||i == 0) ? 'selected' : '') +'>' + c.name + '</option>';
                            lHtml += '<li onclick="position(' + c.id + ')">' + c.name + '</li>';
                        });
                        $("select[name='categoryId']").empty().append(oHtml);
                        $("#catalogList").empty().append(lHtml);

                        form.render('select','update-favorites');
                        form.render('select','add-favorites');
                    }
                }
            });
        };

        window.loadStarFavorites = function () {
            $.ajax({
                type: "GET",
                url: "favorites/star",
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    $("#starDiv").empty();
                    if(result.code == 0 && result.data.length > 0){
                        var html = '';
                        html += '<div class="category">';
                        html += '   <div class="title" style="font-size:16px;">常用网址</div>';
                        html += '   <ul class="favorites">';
                        $.each(result.data, function (i, f) {
                            html += '   <li class="favorites-li layui-anim layui-anim-fadein" data-id="' + f.id + '" data-url="' + f.url + '" data-schema="' + f.schemaName + '" onclick="openUrl(this)">';
                            html += '       <div class="favorites-bg">';
                            html += '           <img src="images/book.svg" lay-src="' + f.icon + '"/>';
                            html += '       </div>';
                            html += '       <p  lay-title="' + f.name + '">' + f.name + '</p>';
                            html += '       <i onclick="editFavorites(this,event)" class="action-icon layui-icon layui-icon-more-vertical"></i>';
                            html += '   </li>';
                        });
                        html += '   </ul>';
                        html += '</div>';

                        var startDiv = $("#starDiv").append(html);
                        loadImg(startDiv);
                    }
                }
            });
        };

        // 显示更多
        window.moreFavorites = function (categoryId) {
            layer.load();
            $.ajax({
                type: "GET",
                url: "favorites/more",
                data: {"categoryId": categoryId},
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    if (result.code == 0) {
                        var html = '';
                        $.each(result.data, function (i, f) {
                            html += '<li class="favorites-li layui-anim layui-anim-fadein" data-id="' + f.id + '" data-url="' + f.url + '" data-schema="' + f.schemaName + '" onclick="openUrl(this)">';
                            html += '   <div class="favorites-bg">';
                            html += '       <img src="images/book.svg" lay-src="' + f.icon + '"/>';
                            html += '   </div>';
                            html += '   <p  lay-title="' + f.name + '">' + f.name + '</p>';
                            html += '   <i onclick="editFavorites(this,event)" class="action-icon layui-icon layui-icon-more-vertical"></i>';
                            html += '</li>';
                        });
                        var favoritesDiv = $("div[data-id='" + categoryId + "']").find(".favorites").empty().append(html);
                        loadImg(favoritesDiv);
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                },
                error: function(){
                    layer.closeAll('loading');
                    layer.msg('服务器异常', {icon: 2});
                }
            });
        };

        // 删除收藏
        $("#deleteFavorites").click(function () {
            layer.confirm('确认删除收藏吗?', function(index){
                layer.close(index);
                layer.load();
                var data = form.val("update-favorites");
                $.ajax({
                    type: "GET",
                    url: "favorites/delete/" + data.id,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        layer.closeAll('loading');
                        layer.close(indexMap.get('#update-favorites'));
                        if (result.code == 0) {
                            window.location.reload();
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
            });
        });

        // 删除快捷导航
        window.deleteFastNav = function(obj, e){
            layui.stope(e);
            layer.confirm('确认删除快捷导航吗?', function(index){
                layer.close(index);
                layer.load();
                $.ajax({
                    type: "GET",
                    url: "quick-navigation/delete/" + $(obj).parent().attr("data-id"),
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        layer.closeAll('loading');
                        if (result.code == 0) {
                            $(obj).parent().remove();
                            $("#addFastNavBtn").show();
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
            });
        };

        // 清空收藏
        $("#clean").click(function () {
            layer.confirm('确认清空分类下所有收藏吗?', function(index){
                layer.close(index);
                layer.load();
                var data = form.val("update-category");
                $.ajax({
                    type: "POST",
                    url: "category/clean",
                    data: {"id": data.id},
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        layer.closeAll('loading');
                        layer.close(indexMap.get('#update-category'));
                        if (result.code == 0) {
                            window.location.reload();
                        } else {
                            layer.msg(result.msg, {icon: 2});
                        }
                    }
                });
            });
        });

        // 删除分类
        $("#deleteCategory").click(function () {
            layer.confirm('确认删除分类吗?', function(index){
                layer.close(index);
                layer.load();
                var data = form.val("update-category");
                $.ajax({
                    type: "GET",
                    url: "category/delete/" + data.id,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        layer.closeAll('loading');
                        layer.close(indexMap.get('#update-category'));
                        if (result.code == 0) {
                            window.location.reload();
                        } else {
                            layer.msg(result.msg, {icon: 2});
                        }
                    }
                });
            });
        });

        // 编辑收藏
        window.editFavorites = function (obj, e) {
            layui.stope(e);
            var index = layer.open({
                type: 1,
                title: "修改收藏",
                skin: 'to-fix-select',
                content: $('#update-favorites')
            });
            indexMap.set('#update-favorites',index);
            // 表单赋值
            $.ajax({
                type: "GET",
                url: "favorites/" + $(obj).parent().attr("data-id"),
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        let data = result.data;
                        //给表单赋值
                        form.val("update-favorites", {
                            "id": data.id
                            , "categoryId": data.categoryId
                            , "name": data.name
                            , "url": data.url
                            , "sort": data.sort
                            , "star": data.star
                            , "isShare": data.isShare
                            , "shortcut": data.shortcut
                            , "schemaName": data.schemaName
                        });
                    }
                }
            });
        };

        form.verify({
            confirmPassword: function (value, item) {
                if($("#newPassword").val() !== value){
                    return "两次输入密码不一致";
                }
            },
            sortNum: function (value, item) {
                if(value != ""){
                    if(isNaN(value)){
                        return "请输入0~9999之间的整数";
                    }else if(value%1 !== 0){
                        return "请输入0~9999之间的整数";
                    }else if(value > 9999){
                        return "请输入0~9999之间的整数";
                    }
                }
            },
            exitEmail: function (value, item) {
                var msg;
                $.ajax({
                    type: "GET",
                    url: 'register/email/' + value,
                    async: false,
                    dataType: 'json',
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code != 0) {
                            msg = "邮箱已存在";
                        }
                    }
                });
                return msg;
            },
            shortcut: function (value, item) {
                if(value !== ""){
                    if(!value.indexOf("打开") == 0 && !value.indexOf("Open") == 0){
                        return '指令请以"打开"或"Open"开头';
                    }
                    var msg;
                    $.ajax({
                        type: "GET",
                        url: 'favorites/shortcut?key=' + value,
                        async: false,
                        dataType: 'json',
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            if (result.code == 0 && result.data.id != $("#favoritesId").val()) {
                                msg = "指令已存在";
                            }
                        }
                    });
                    return msg;
                }
            },
            category: function (value, item) {
                var categoryId = form.val("update-category").id;
                var msg;
                $.ajax({
                    type: "GET",
                    url: 'category/check/' + value,
                    async: false,
                    dataType: 'json',
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0 && result.data.id != categoryId) {
                            msg = "分类已存在";
                        }
                    }
                });
                return msg;
            }
        });

        form.on('switch(viewStyle)', function(data){
            if(data.elem.checked){
                $("#layuiBody").addClass("bookmark");
            }else{
                $("#layuiBody").removeClass("bookmark");
            }
            $.ajax({
                type: "POST",
                url: "user/style",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                data: {"viewStyle": data.elem.checked? 1 : 0}
            });
        });

        form.on('switch(starSwitch)', function(data){
            layer.load();
            $.ajax({
                type: "POST",
                url: "favorites/star",
                data: JSON.stringify({"id": $("#favoritesId").val(),"star": data.elem.checked? 1 : 0}),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    if (result.code == 0) {
                        layer.msg("操作成功", {icon: 6});
                        loadStarFavorites();
                        // 退出搜索模式
                        $("#search_close").click();
                    } else {
                        layer.msg(result.msg, {icon: 5});
                        // 回退
                        $(data.elem).removeAttr("checked");
                        $(data.othis).removeClass("layui-form-onswitch");
                    }
                },
                error: function(){
                    layer.closeAll('loading');
                    layer.msg('服务器异常', {icon: 2});
                    // 回退
                    $(data.elem).removeAttr("checked");
                    $(data.othis).removeClass("layui-form-onswitch");
                }
            });
        });

        form.on('switch(shareSwitch)', function(data){
            layer.load();
            $.ajax({
                type: "POST",
                url: "favorites/share",
                data: JSON.stringify({"id": $("#favoritesId").val(),"isShare": data.elem.checked? 1 : 0}),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    if (result.code == 0) {
                        layer.msg("操作成功", {icon: 6});
                    } else {
                        layer.msg(result.msg, {icon: 5});
                        // 回退
                        $(data.elem).removeAttr("checked");
                        $(data.othis).removeClass("layui-form-onswitch");
                    }
                },
                error: function(){
                    layer.closeAll('loading');
                    layer.msg('服务器异常', {icon: 2});
                    // 回退
                    $(data.elem).removeAttr("checked");
                    $(data.othis).removeClass("layui-form-onswitch");
                }
            });
        });

        form.on('switch(bookmarkSwitch)', function(data){
            var categoryId = form.val("update-category").id;
            layer.load();
            $.ajax({
                type: "POST",
                url: "category/bookmark",
                data: JSON.stringify({"id": categoryId,"bookmark": data.elem.checked? 1 : 0}),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    if (result.code == 0) {
                        layer.msg("操作成功", {icon: 6});
                        if(data.elem.checked){
                             $("div[data-id=" + categoryId + "]").addClass("bookmark");
                        }else{
                             $("div[data-id=" + categoryId + "]").removeClass("bookmark");
                        }
                    } else {
                        layer.msg(result.msg, {icon: 5});
                        // 回退
                        $(data.elem).removeAttr("checked");
                        $(data.othis).removeClass("layui-form-onswitch");
                    }
                },
                error: function(){
                    layer.closeAll('loading');
                    layer.msg('服务器异常', {icon: 2});
                    // 回退
                    $(data.elem).removeAttr("checked");
                    $(data.othis).removeClass("layui-form-onswitch");
                }
            });
        });

        // 新增收藏
        form.on('submit(addFavorites)', function (data) {
            data.field.name = escapeHtml(data.field.name);
            layer.load();
            $.ajax({
                type: "POST",
                url: "favorites/save",
                data: JSON.stringify(data.field),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    layer.close(indexMap.get('#add-favorites'));
                    if (result.code == 0) {
                        window.location.reload();
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
            return false;
        });

        // 新增快捷导航
        form.on('submit(addFastNav)', function (data) {
            layer.load();
            $.ajax({
                type: "POST",
                url: "quick-navigation",
                data: JSON.stringify(data.field),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    layer.close(indexMap.get('#add-favorites'));
                    if (result.code == 0) {
                        window.location.reload();
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
            return false;
        });

        // 保存密码
        form.on('submit(savePwd)', function (data) {
            if(!data.field.account && !data.field.password){
                return false;
            }
            layer.load();
            $.ajax({
                type: "POST",
                url: "password",
                data: JSON.stringify(data.field),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    if (result.code == 0) {
                        //给表单赋值
                        form.val("setting-pwd", {
                            "id": result.data
                        });
                        layer.msg("保存成功", {icon: 6});
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
            return false;
        });

        // 修改邮箱
        form.on('submit(updateEmail)', function (data) {
            layer.load();
            $.ajax({
                type: "POST",
                url: "user/email",
                data: data.field,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    layer.close(indexMap.get('#editEmail'));
                    if (result.code == 0) {
                        layer.msg("修改成功", {icon: 6});
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
            return false;
        });

        // 修改收藏
        form.on('submit(updateFavorites)', function (data) {
            data.field.name = escapeHtml(data.field.name);
            layer.load();
            $.ajax({
                type: "POST",
                url: "favorites/save",
                data: JSON.stringify(data.field),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    layer.close(indexMap.get('#update-favorites'));
                    if (result.code == 0) {
                        window.location.reload();
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
            return false;
        });

        // 修改分类
        form.on('submit(updateCategory)', function (data) {
            data.field.name = escapeHtml(data.field.name);
            layer.load();
            $.ajax({
                type: "POST",
                url: "category",
                data: JSON.stringify(data.field),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    layer.close(indexMap.get('#update-category'));
                    if (result.code == 0) {
                        window.location.reload();
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
                    layer.close(indexMap.get('#changePwd'));
                    if (result.code == 0) {
                        layer.msg('修改成功', {icon: 6, time: 1000}, function(){
                            localStorage.clear();
                            window.location.href = "login.html";
                        });
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
            return false;
        });

        // 搜索项绑定hover事件
        $(document).on('mouseenter', '.search-items li', function() {
            $(this).addClass('hover-in');
        });

        $(document).on('mouseleave', '.search-items li', function() {
            $(this).removeClass('hover-in');
        });

        window.position = function(id){
            changePositionMode();
            layer.close(indexMap.get('#catalog'));
            layer.load();
            $.ajax({
                type: "GET",
                url: "favorites/position/" + id,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    if (result.code == 0) {
                        // 加载数据
                        var c = result.data;
                        var html = '';
                        var bookmarkClass = c.bookmark==1?"bookmark":"";
                        html += '<div class="category ' + bookmarkClass + '" data-id="' + c.id + '">';
                        html += '   <div class="title" onclick="editCategory(this,event)">' + c.name + '</div>';
                        html += '   <ul class="favorites">';
                        if(c.favorites.length > 0) {
                            $.each(c.favorites, function (j, f) {
                                html += '   <li class="favorites-li layui-anim layui-anim-fadein" data-id="' + f.id + '" data-url="' + f.url + '" data-schema="' + f.schemaName + '" onclick="openUrl(this)">';
                                html += '       <div class="favorites-bg">';
                                html += '           <img src="images/book.svg" lay-src="' + f.icon + '"/>';
                                html += '       </div>';
                                html += '       <p  lay-title="' + f.name + '">' + f.name + '</p>';
                                html += '       <i onclick="editFavorites(this,event)" class="action-icon layui-icon layui-icon-more-vertical"></i>';
                                html += '   </li>';
                                if(j == favoritesLimit - 1){
                                    html += '<li class="layui-anim layui-anim-fadein" onclick="moreFavorites(' + c.id + ')">';
                                    html += '   <div class="favorites-bg">';
                                    html += '       <img src="images/more.svg"/>';
                                    html += '   </div>';
                                    html += '   <p lay-title="显示更多">显示更多</p>';
                                    html += '</li>';
                                    return false;
                                }
                            });
                        } else {
                            html += '<li class="layui-anim layui-anim-fadein" onclick="addFavorites(' + c.id + ')">';
                            html += '   <div class="favorites-bg">';
                            html += '       <img src="images/add.svg"/>';
                            html += '   </div>';
                            html += '   <p lay-title="添加网址">添加网址</p>';
                            html += '</li>';
                        }
                        html += '   </ul>';
                        html += '</div>';
                        var positionData = $("#positionData").empty().append(html);
                        loadImg(positionData);
                    }
                },
                error: function(){
                    layer.closeAll('loading');
                    layer.msg("服务器异常", {icon: 2});
                }
            });
        };

        window.loadUserInfo = function(){
            $.ajax({
                type: "GET",
                url: "user/info",
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        var user = result.data;
                        if(!user.nickName){
                            window.location.href = "set_user_info.html";
                        }
                        $("#username").text(user.nickName.substring(0, 4));
                        form.val("styleSelect", {"viewStyle": user.viewStyle});
                        if(user.viewStyle == 1)$("#layuiBody").addClass("bookmark");
                    }
                }
            });
        };

        window.initClipboard = function(){
            var clipboard = new ClipboardJS('#accountCopy');
            clipboard.on('success', function(e) {layer.msg("复制成功");});
            clipboard.on('error', function(e) {layer.msg("复制失败");});

            var clipboard1 = new ClipboardJS('#passwordCopy');
            clipboard1.on('success', function(e) {layer.msg("复制成功");});
            clipboard1.on('error', function(e) {layer.msg("复制失败");});

            var clipboard2 = new ClipboardJS('#edit_url_input_copy');
            clipboard2.on('success', function(e) {layer.msg("复制成功");});
            clipboard2.on('error', function(e) {layer.msg("复制失败");});
        };

        $("#slideBtn").click(function(){
            var width = (windowWidth >= 500 ? 500 : (windowWidth <= 340 ? 340 : windowWidth)) + 'px';
            var index = layer.open({
                type: 1,
                title: "目录",
                area: [width, '300px'],
                content: $("#catalog")
            });
            indexMap.set('#catalog',index);
        });

        // 回收站
        $("#recycle").click(function() {
            var width = (windowWidth >= 800? 800 : windowWidth) + 'px';
            var index = layer.open({
                type: 2,
                area: [width,'530px'],
                title: false,
                closeBtn: 0,
                scrollbar: false,
                content: ['recycle.html?' + timeSuffix(),'no']
            });
            indexMap.set('#importOrExport',index);
        });

        // 报告
        $("#report").click(function() {
            var width = (windowWidth >= 820? 820 : windowWidth) + 'px';
            var index = layer.open({
                type: 2,
                area: [width,'530px'],
                title: false,
                closeBtn: 0,
                content: 'report.html?' + timeSuffix()
            });
            indexMap.set('#report',index);
        });

        window.escapeHtml = function(str){
             return str.replace(/[<>&"\s\n]/ig,function(c){return {'<':'&lt;','>':'&gt;','&':'&amp;','"':'&quot;',' ':'&nbsp;','\n':'<br/>'}[c];});
        };

        window.showNotice = function(type){
            // 查询公告
            $.ajax({
                type: "GET",
                url: "user/notice",
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    var data = result.data;
                    if (data.isShow) {
                        // 显示按钮
                        if(windowWidth >= 550){
                            $("#notice cite").text(data.title);
                            $("#notice").show();
                        }
                        if(type == "ready" && localStorage.getItem("notShowNotice")){
                            return false;
                        }
                        // 弹出公告
                        var width = (windowWidth >= 970 ? 970 : windowWidth) + 'px';
                        var index = layer.open({
                            title: data.title
                            ,content: data.content
                            ,area: [width, '375px']
                            ,btn: ['知道了']
                            ,yes: function(index, layero){
                                layer.close(index);
                                localStorage.setItem("notShowNotice", true);
                            }
                        });
                        indexMap.set('#showNotice',index);
                    }else{
                        if(type == "click"){
                            layer.alert('暂无公告！', {icon: 0});
                        }else{
                            $("#notice").hide();
                        }
                    }
                }
            });
        };

        // 初始化title
        $(document).on('mouseenter','[lay-title]',function(e){
            var that = $(e.currentTarget);
            layer.tips(that.attr("lay-title"), that, {tips: 3, time: 0});
        }).on('mouseleave','[lay-title]',function(e){
            layer.closeAll('tips');
        });

        // 定时器
        window.onlineCount = function(){
            $.ajax({
                type: "GET",
                url: "user/online",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            });
        };
        setInterval(onlineCount(),3600000);

        // 页面加载完成执行
        $(function () {
            // 加载用户信息
            loadUserInfo();
            // 加载常用网址
            loadStarFavorites();
            // 加载分类
            loadCategoryList();
            // 初始化剪贴板插件
            initClipboard();
            // 显示公告
            showNotice('ready');
        });
    });
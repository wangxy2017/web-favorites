layui.use(['layer', 'form', 'flow'], function(){
            var layer = layui.layer;
            var form = layui.form;
            var flow = layui.flow;

            // 点击空白关闭
            $(document).on("click", function(e) {
                var _conss = $('.search-form');//点击的容器范围
                if (!_conss.is(e.target) && _conss.has(e.target).length === 0) {
                    $(".search-items").hide();
                    $("#search_close").hide();
                }
            });

            // 加载数据
            $('#searchTypeList').unbind();
            flow.load({
                elem: '#searchTypeList'
                ,scrollElem: '#searchTypeList'
                ,isLazyimg: true
                ,mb: 50
                ,end: ' '
                ,done: function(page, next){
                  var lis = [];
                  if(page == 1){
                    $.ajax({
                        type: "GET",
                        url: "search/system",
                        async: false,
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                           $.each(result.data, function(i, item){
                                var html = '';
                                html += '<li class="search-type-li layui-anim layui-anim-fadein" data-search="' + item.url + '"><img src="images/web.svg" lay-src="' + item.icon + '"><span>' + item.name +
                                    '</span></li>';
                                lis.push(html);
                           });
                        }
                    });
                  }
                    $.ajax({
                        type: "GET",
                        url: "search/data",
                        data: {"pageNum": page,"pageSize": 20},
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            if(result.code == 0){
                                $.each(result.data.list, function(index, item) {
                                    var html = '';
                                    html += '<li class="search-type-li layui-anim layui-anim-fadein" data-id="' + item.id + '" data-search="' + item.url + '"><img src="images/web.svg" lay-src="' + item.icon + '"><span>' + item.name +
                                        '</span><i class="layui-icon layui-icon-close" onclick="deleteSearchType(event)"></i></li>';
                                    lis.push(html);
                                });
                                $("#addBtn").remove();
                                var addBtn = '<li id="addBtn" onclick="addSearchType()"><img src="images/add.svg"><span>添加搜索</span></li>';
                                lis.push(addBtn);
                                next(lis.join(''), page < result.data.pages);
                            }
                        }
                    });
                }
            });

            // 初始化title
            $(document).on('mouseenter','[lay-title]',function(e){
                var that = $(e.currentTarget);
                layer.tips(that.attr("lay-title"), that, {tips: 3, time: 0});
            }).on('mouseleave','[lay-title]',function(e){
                layer.closeAll('tips');
            });

            $(document).on("keydown", function(event){
                if(event.ctrlKey && event.key === "f"){
                    $("#search_content").focus();
                    // 阻止默认浏览器动作(W3C)
                    var e = event;
                    if ( e && e.preventDefault )
                        e.preventDefault();
                    // IE中阻止函数器默认动作的方式
                    else
                        window.event.returnValue = false;
                    return false;
                }
            });

            // 根据数组的下标，删除该下标的元素
            Array.prototype.remove = function(val) {
                var index = this.indexOf(val);
                if (index > -1) {
                    this.splice(index, 1);
                }
            };

            // 登出
            $("#logout").click(function () {
                layer.confirm('确认退出系统吗？', function(index){
                    layer.close(index);

                    localStorage.clear();
                    window.location.href = "login.html";
                });
            });

            // 删除搜索引擎
			window.deleteSearchType = function(event){
			    layui.stope(event);
                var id = $(event.target).parent().attr("data-id");
			    layer.confirm('确认删除搜索引擎吗?', function(index){
                    layer.close(index);
                    layer.load();
                    $.ajax({
                        type: "GET",
                        url: "search/delete/" + id,
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            layer.closeAll('loading');
                            if (result.code == 0) {
                                window.location.reload();
                            } else {
                                layer.msg("删除失败", {icon: 5});
                            }
                        }
                    });
                });
			};

			// 添加搜索引擎
			window.addSearchType = function(){
			    var index = layer.open({
                    type: 1,
                    title: "添加搜索引擎",
                    content: $('#searchTypeFrom')
                });
                indexMap.set('#searchTypeFrom',index);
                // 表单赋值
                form.val("searchTypeFrom", {
                    "name": ""
                    , "url": ""
                });
			};

			//监听提交
            form.on('submit(addSearchType)', function(data){
                layer.load();
                $.ajax({
                    type: "POST",
                    url: "search",
                    data: JSON.stringify(data.field),
                    contentType: 'application/json;charset=utf-8',
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        layer.closeAll('loading');
                        layer.close(indexMap.get('#searchTypeFrom'));
                        if (result.code == 0) {
                            window.location.reload();
                        } else {
                            layer.msg("添加失败", {icon: 5});
                        }
                    }
                });
                return false;
            });

            form.verify({
              name: function (value, item) {
                if(value.length>20){
                    return '最多输入20个字符';
                }
              }
            });

			$("#cancelAddSearchType").click(function () {
                layer.close(indexMap.get('#searchTypeFrom'));
            });

			// 搜索类型绑定点击事件
			$(document).on('click', '.search-type-li', function() {
				$(this).addClass('search-type-selected').siblings().removeClass('search-type-selected');
			});

			// 搜索项绑定hover事件
			$(document).on('mouseenter', '.search-items li', function() {
				$(this).addClass('hover-in');
			});

			$(document).on('mouseleave', '.search-items li', function() {
				$(this).removeClass('hover-in');
			});

			// 搜索项绑定点击事件
			$(document).on('click', '.search-items li', function() {
				$("#search_content").val($(this).text());
				$(this).parent().hide();
				$("#search_btn").trigger("click");
			});

			$(document).on('click', '.search-items li .history-close', function(event) {
                layui.stope(event);

                $(this).parent().remove();
                if($(".search-items").children().length === 0) $(".search-items").hide();

                var text = $(this).parent().text();
                var search_history = JSON.parse(localStorage.getItem("search_history"));
                if(search_history.indexOf(text) >= 0) {
                    search_history.remove(text);
                    localStorage.setItem("search_history", JSON.stringify(search_history));
                }
            });

			$("#search_close").click(function() {
			    $("#search_content").val("");
			    $(".search-items").hide();
			    $(this).hide();
			});

			// 点击搜索按钮搜索
			$("#search_btn").click(function() {
				var content = $("#search_content").val().trim();
				if (content != "") {
					// 存入历史记录
					var search_history = JSON.parse(localStorage.getItem("search_history"));
					if (search_history == null || search_history.length >= 10) search_history = [];
					if (search_history.indexOf(content) < 0) search_history.push(content);
					localStorage.setItem("search_history", JSON.stringify(search_history));
					// 跳转页面
					var search_url = $(".search-type .search-type-li.search-type-selected").attr("data-search");
					search_url = typeof search_url === "undefined" ? "https://www.baidu.com/s?wd=" : search_url;
					openUrl(search_url + content);
				} else {
					window.location.reload();
				}
			});

			$("#search_content").keyup(function(event) {
				if (event.key === "Enter") { // 回车搜索，隐藏搜索项
					$("#search_btn").trigger("click");
					$(".search-items").hide();
				} else if (event.key === "ArrowUp") {
					var hover_in = typeof $(".search-items li.hover-in")[0] === "undefined" ? $(".search-items li:last")[0] : $(
						".search-items li.hover-in").prev()[0];
					if (typeof hover_in !== "undefined") {
						$(hover_in).addClass('hover-in').siblings().removeClass('hover-in');
						$(this).val(hover_in.innerText);
					}
				} else if (event.key === "ArrowDown") {
					var hover_in = typeof $(".search-items li.hover-in")[0] === "undefined" ? $(".search-items li:first")[0] : $(
						".search-items li.hover-in").next()[0];
					if (typeof hover_in !== "undefined") {
						$(hover_in).addClass('hover-in').siblings().removeClass('hover-in');
						$(this).val(hover_in.innerText);
					}
				}
			}).bind("input propertychange", function() { // 输入框输入值，查询百度词条
				var keyword = $(this).val().trim();
				if (keyword !== "") {
					$.ajax({
						url: "https://www.baidu.com/su?wd=" + keyword,
						type: "GET",
						dataType: "jsonp", //指定服务器返回的数据类型
						jsonp: "cb",
                        success: function(data) {
							var html = '';
							$.each(data.s, function(index, item) {
								html += '<li>' + item + '</li>';
							});
							if (html != "") $(".search-items").empty().append(html).show();
						}
					});
				}
			}).focus(function() { // 输入框聚焦，显示历史搜索
		        $("#search_close").show();
		        var html = '';
				var search_history = JSON.parse(localStorage.getItem("search_history"));
				$.each(search_history, function(index, item) {
					html = '<li>' + item + '<i class="layui-icon layui-icon-close history-close"></i></li>' + html;
				});
				if (html !== "") $(".search-items").empty().append(html).show();
			});

			window.openUrl = function(url) {
				if (url.indexOf("https://") == 0 || url.indexOf("http://") == 0) {
					newWin(url);
					// 记录搜索次数
					$.ajax({
                        type: "GET",
                        url: "user/search",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
                    });
				} else {
					alert("此链接无效");
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
        });
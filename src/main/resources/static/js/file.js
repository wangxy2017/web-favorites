layui.use(['element', 'layer', 'table', 'upload', 'tree'], function() {
        var element = layui.element;
        var layer = layui.layer;
        var table = layui.table;
        var upload = layui.upload;
        var tree = layui.tree;
        // 全局变量
        var treeData = [];

        //加载数据
        table.render({
            elem: '#fileList'
            , url: 'file/list/' //数据接口
            , headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            , page: {
                layout: ['count', 'prev', 'page', 'next', 'skip']
                , prev: '上一页'
                , next: '下一页'
                , limit: 10
            }
            , request: {
                pageName: 'pageNum' //页码的参数名称，默认：page
                , limitName: 'pageSize' //每页数据量的参数名，默认：limit
            }
            , parseData: function (res) { //res 即为原始返回的数据
                return {
                    "code": res.code, //解析接口状态
                    "msg": res.msg, //解析提示文本
                    "count": res.data.page.total,
                    "data": res.data.page.list, //解析数据列表
                    "parent": res.data.parent,
                    "floors": res.data.floors
                };
            }
            , done: function (res, curr, count) {
                $("#parent").val(res.parent);
                var html = '';
                $(res.floors).each(function(index,item){
                    html += '<a href="javascript:position('+ item.id +')">'+ escape(item.filename) +'</a>';
                });
                $("#floors").empty().append('<a href="">根目录</a>').append(html);
                element.render('breadcrumb','floors');
                // 解决无数据table样式错乱
                if(count == 0){
                    $("th[data-key='1-0-4']").css("border-right", "0");
                }
            }
            , cols: [[ //表头
                {type: 'checkbox'}
                , {
                    field: 'filename', title: '目录/文件', minWidth: 200, templet: function (d) {
                        var html = '<div class="file-info">';
                        if (d.isDir == 1) {
                            html += '<a class="layui-table-link" onclick="goto(this)" data-id="' + d.id + '" data-directory="' + (d.isDir == 1) + '"><img class="file-icon" src="images/folder.svg"/>' + escape(d.filename) + '</a>';
                        } else {
                            html += '<a class="layui-table-link" onclick="goto(this)" data-id="' + d.id + '" data-directory="' + (d.isDir == 1) + '"><img class="file-icon" src="' + suffix(d.filename) + '"/>' + escape(d.filename) + '</a>';
                            html += '<i class="file-share layui-icon layui-icon-release" onclick="share(this)" data-id="' + d.id + '" title="分享文件"></i>';
                        }
                        html += '</div>';
                        return html;
                    }
                }
                , {
                    field: 'size', title: '大小', templet: function (d) {
                        return d.isDir == 1 ? "0 B" : change(d.size);
                    }
                }
                , {
                    field: 'updateTime', title: '上传时间'
                }
                , {title: '操作', width: 170, toolbar: '#operates', fixed: 'right'}
            ]]
        });

        // 加载文件夹树
        window.loadTree = function(){
            $.ajax({
                type: "GET",
                url: "file/tree",
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        treeData = result.data;
                        // 渲染文件夹树结构
                        tree.render({
                            elem: '#folderTree'
                            , id: 'demoId'
                            , data: treeData
                            , showLine: false
                            , onlyIconControl: true
                            , click: function (obj) {
                                layer.confirm('确认移动到此文件夹吗?', function (index) {
                                    var checkStatus = table.checkStatus('fileList');
                                    var ids = [];
                                    $.each(checkStatus.data, function (index, item) {
                                        ids.push(item.id);
                                    });
                                    $.ajax({
                                        type: "POST",
                                        url: "file/move",
                                        data: {"ids": ids.join(","), "pid": obj.data.id},
                                        dataType: "json",
                                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                                        success: function (result) {
                                            if (result.code == 0) {
                                                layer.msg('操作成功', {icon: 6});
                                                table.reload('fileList');
                                                layer.close(indexMap.get('#folderTree'));
                                            } else {
                                                layer.msg(result.msg, {icon: 5});
                                            }
                                        }
                                    });
                                });
                            }
                        });
                    }
                }
            });
        };

        $("#upload").click(function(){
            var width = (windowWidth > 800? 800 : windowWidth) + 'px';
            var index = layer.open({
                type: 1,
                title: "上传文件",
                area: [width,'530px'],
                content: $("#layuiUpload")
            });
            indexMap.set('#layuiUpload',index);
        });

        initSearch("#searchName");

        window.loadCapacity = function(){
            $.ajax({
                type: "GET",
                url: "file/capacity",
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        var data = result.data;
                        $("#used").text(change(data.usedSize));
                        $("#rest").text(change(data.capacity - data.usedSize));
                        uploadListIns.reload({size: (data.capacity - data.usedSize) / 1024});
                    }
                }
            });
        };

        $("#downloadAll").click(function(){
            layer.confirm('确认备份所有文件吗？', function(index){
                layer.load(1);
                $.ajax({
                    type: "GET",
                    url: "file/count",
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0 && result.data.count > 0) {
                            downloadFile('备份.zip', 'file/downloadAll');
                        } else {
                            layer.closeAll('loading');
                            layer.msg('暂无可备份文件', {icon: 5});
                        }
                    }
                });

                layer.close(index);
            });
        });

        // 页面加载后执行
        $(function () {
            loadCapacity();
            loadTree();
        });

        // 移动
        $("#move").click(function () {
            var checkStatus = table.checkStatus('fileList');
            if (checkStatus.data.length === 0) {
                layer.msg('请选择文件');
                return false;
            }
            // 禁用部分文件夹
            var dirs = [];
            $.each(checkStatus.data, function(i, item){
                if(item.isDir == 1){
                    dirs[i] = item.id;
                }
            });
            var newData = getNewData(dirs, treeData);
            tree.reload('demoId', {data: newData});
            // 加载文件树
            var index = layer.open({
                type: 1,
                title: "移动文件(夹)",
                area: [windowWidth < 800 ? '360px' : '400px', '300px'],
                content: $('#folderTree')
            });
            indexMap.set('#folderTree',index);
        });

        window.getNewData = function(dirs, data, disabled){
            var newData = [];
            $.each(data, function(i, item){
                var n = {};
                n.id = item.id;
                n.title = item.title;
                if(dirs.includes(item.id) || disabled){
                    n.disabled = true;
                }
                n.children = getNewData(dirs, item.children, n.disabled);
                newData[i] = n;
            });
            return newData;
        };

        // 全部删除
        $("#delAll").click(function () {
            var checkStatus = table.checkStatus('fileList');
            if (checkStatus.data.length === 0) {
                layer.msg('请选择文件');
                return false;
            }
            layer.confirm('确认删除全部文件吗?', function (index) {
                var ids = [];
                $.each(checkStatus.data, function (index, item) {
                    ids.push(item.id);
                });
                $.ajax({
                    type: "POST",
                    url: "file/deleteMore",
                    data: {"ids": ids.join(",")},
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            layer.msg('删除成功', {icon: 6});
                            table.reload('fileList');
                            loadCapacity();
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
                layer.close(index);
            });
        });

        window.searchCount = function(){
            // 记录搜索次数
            $.ajax({
                type: "GET",
                url: "user/search",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            });
        };

        // 搜索
        $('#searchName').bind('keypress', function (event) {
            if (event.key === "Enter") {
                $("#search").click();
            }
        });

        $("#search").click(function(){
            var name = $("#searchName").val();
            table.reload('fileList', {page: {curr: 1}, where: {"name": name, "pid": null}});
            if(name != ""){
                searchCount();
            }
        });

        // 返回
        $("#goBack").click(function () {
            $.ajax({
                type: "GET",
                url: "file/back",
                data: {"pid": $("#parent").val()},
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        var pid = result.data ? result.data : null;
                        table.reload('fileList', {page: {curr: 1}, where: {"pid": pid, "name": ""}});
                    }
                }
            });

        });
        // 新建文件夹
        $("#newFolder").click(function () {
            layer.prompt({
                title: '新建文件夹',
                placeholder:"输入文件夹名称",
                maxlength: 100
            }, function (value, index, elem) {
                $.ajax({
                    type: "POST",
                    url: "file/folder",
                    data: {
                        "pid": $("#parent").val(),
                        "filename": value
                    },
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            table.reload('fileList');
                            loadTree();
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
                layer.close(index);
            });
        });


        //监听工具条
        table.on('tool(fileList)', function (obj) {
            var data = obj.data;
            var layEvent = obj.event;
            if (layEvent === 'edit') { //修改
                layer.prompt({
                    title: '重命名文件(夹)',
                    value: data.filename,
                    maxlength: 100,
                }, function (value, index, elem) {
                    $.ajax({
                        type: "POST",
                        url: "file/rename",
                        data: {"id": data.id, "filename": value},
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            if (result.code == 0) {
                                layer.msg('修改成功', {icon: 6});
                                //同步更新缓存对应的值
                                obj.update({filename: value});
                            } else {
                                layer.msg(result.msg, {icon: 5});
                            }
                        }
                    });
                    layer.close(index);
                });
            } else if (layEvent === 'del') { //删除
                layer.confirm('确认删除文件吗？', function (index) {
                    $.ajax({
                        type: "POST",
                        url: "file/delete",
                        data: {"id": data.id},
                        dataType: "json",
                        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                        success: function (result) {
                            if (result.code == 0) {
                                obj.del(); //移除当前行
                                layer.msg('删除成功', {icon: 6});
                                loadCapacity();
                            } else {
                                layer.msg(result.msg, {icon: 5});
                            }
                        }
                    });
                    layer.close(index);
                });
            } else if (layEvent === 'download') { //下载
                if (data.isDir == 1) {
                    layer.msg('暂不支持文件夹下载', {icon: 5});
                } else {
                    layer.confirm('确认下载文件吗？', function(index){
                        layer.close(index);

                        layer.load(1);
                        $.ajax({
                            type: "GET",
                            url: "file/exists/" + data.id,
                            dataType: "json",
                            headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                            success: function (result) {
                                if (result.code == 0) {
                                    downloadFile(escape(data.filename), "file/download/" + data.id);
                                } else {
                                    layer.closeAll('loading');
                                    layer.msg('文件已损坏', {icon: 5});
                                }
                            }
                        });
                    });
                }
            }
        });

          //演示多文件列表
          var uploadListIns = upload.render({
            elem: '#testList'
            ,elemList: $('#demoList') //列表元素对象
            , url: 'file/upload/' //上传接口
            , headers: {"Authorization": "Bearer "+ localStorage.getItem("login_user_token")}
            , data: {
                "pid": function () {
                    return $("#parent").val();
                }
            }
            ,accept: 'file'
            ,multiple: true
            ,auto: false
            ,bindAction: '#testListAction'
            ,choose: function(obj){
              var that = this;
              var files = this.files = obj.pushFile(); //将每次选择的文件追加到文件队列
              //读取本地文件
              obj.preview(function(index, file, result){
                var tr = $(['<tr id="upload-'+ index +'">'
                  ,'<td>'+ file.name +'</td>'
                  ,'<td>'+ change(file.size) +'</td>'
                  ,'<td><div class="layui-progress" lay-filter="progress-demo-'+ index +'"><div class="layui-progress-bar" lay-percent=""></div></div></td>'
                  ,'<td>'
                    ,'<button class="layui-btn layui-btn-xs demo-reload layui-hide">重传</button>'
                    ,'<button class="layui-btn layui-btn-xs layui-btn-danger demo-delete">删除</button>'
                  ,'</td>'
                ,'</tr>'].join(''));

                //单个重传
                tr.find('.demo-reload').on('click', function(){
                  obj.upload(index, file);
                });

                //删除
                tr.find('.demo-delete').on('click', function(){
                  delete files[index]; //删除对应的文件
                  tr.remove();
                  uploadListIns.config.elem.next()[0].value = ''; //清空 input file 值，以免删除后出现同名文件不可选
                });

                that.elemList.append(tr);
                element.render('progress'); //渲染新加的进度条组件
              });
            }
            ,done: function(res, index, upload){ //成功的回调
              var that = this;
              if(res.code == 0){ //上传成功
                var tr = that.elemList.find('tr#upload-'+ index)
                ,tds = tr.children();
                tds.eq(3).html(''); //清空操作
                delete this.files[index]; //删除文件队列已经上传成功的文件
                return;
              }
              this.error(index, upload);
            }
            ,allDone: function(obj){ //多文件上传完毕后的状态回调
              table.reload('fileList');
              loadCapacity();
            }
            ,error: function(index, upload){ //错误回调
              var that = this;
              var tr = that.elemList.find('tr#upload-'+ index)
              ,tds = tr.children();
              tds.eq(3).find('.demo-reload').removeClass('layui-hide'); //显示重传
            }
            ,progress: function(n, elem, e, index){ //注意：index 参数为 layui 2.6.6 新增
              element.progress('progress-demo-'+ index, n + '%'); //执行进度条。n 即为返回的进度百分比
              if(n == 100){
                $("#upload-" + index).find(".layui-progress-bar").attr("lay-percent","100%");
              }
            }
          });



        window.copyText = function(value){
            var input = document.createElement('input');
            input.setAttribute('readonly', 'readonly');
            input.setAttribute('value', value);
            document.body.appendChild(input);
            input.select();
            if (document.execCommand('copy')) {
                document.execCommand('copy');
            }
            document.body.removeChild(input);
        };

        // 分享
        window.share = function (obj) {
            var id = $(obj).attr("data-id");
            layer.load(1);
            $.ajax({
                type: "GET",
                url: "file/share/" + id,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    if (result.code == 0) {
                        var url = window.location.href;
                        var path = url.substring(0,url.lastIndexOf("/"));
                        var text = path + "/file/share/download/" + result.data;
                        layer.open({
                          title: '复制以下链接分享'
                          ,content: text
                          ,btn: ['复制', '取消分享']
                          ,yes: function(index, layero){
                            copyText(text);
                            layer.msg("复制成功");
                            layer.close(index);
                          }
                          ,btn2: function(index, layero){
                            $.ajax({
                                type: "GET",
                                url: "file/share/cancel/" + id,
                                dataType: "json",
                                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                                success: function (result) {
                                    if (result.code == 0) {
                                        layer.msg("操作成功");
                                    }
                                }
                            });
                            layer.close(index);
                          }
                        });
                    } else {
                        layer.msg('文件已损坏', {icon: 5});
                    }
                }
            });
        };

        window.position = function (id) {
            table.reload('fileList', {page: {curr: 1}, where: {"pid": id}});
        }

        // 跳转
        window.goto = function (obj) {
            var that = $(obj);
            if (that.attr("data-directory") === "true") {
                table.reload('fileList', {page: {curr: 1}, where: {"pid": that.attr("data-id")}});
            } else {
                var id = that.attr("data-id");
                $.ajax({
                    type: "GET",
                    url: "file/view",
                    data: {"id": id},
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            var width = (windowWidth > 800? 800 : windowWidth) + 'px';
                            layer.open({
                                area:[width,'600px'],
                                type: 1,
                                title: '文件内容',
                                content: '<pre style="padding: 10px;margin: 0">' + escape(result.data) + '</pre>'
                            });
                        } else {
                            layer.msg('暂不支持文件查看', {icon: 5});
                        }
                    }
                });
            }
        };

        // 判断后缀
        window.suffix = function (url) {
            var suffix = url.substring(url.lastIndexOf("."));
            if (suffix === ".bat")
                return "images/bat.svg";
            else if (suffix === ".xls" || suffix === ".xlsx")
                return "images/excel.svg";
            else if (suffix === ".exe")
                return "images/exe.svg";
            else if (suffix === ".md")
                return "images/markdown.svg";
            else if (suffix === ".mp3" || suffix === ".wma")
                return "images/music.svg";
            else if (suffix === ".pdf")
                return "images/pdf.svg";
            else if (suffix === ".png" || suffix === ".jpg" || suffix === ".ico")
                return "images/photo.svg";
            else if (suffix === ".gif")
                return "images/gif.svg";
            else if (suffix === ".svg")
                return "images/svg.svg";
            else if (suffix === ".ppt" || suffix === ".pptx")
                return "images/ppt.svg";
            else if (suffix === ".sh")
                return "images/shell.svg";
            else if (suffix === ".gz" || suffix === ".tar" || suffix ===".tgz" || suffix === ".xz")
                return "images/tar.svg";
            else if (suffix === ".txt")
                return "images/txt.svg";
            else if (suffix === ".properties")
                return "images/properties.svg";
            else if (suffix === ".yml")
                return "images/yml.svg";
            else if (suffix === ".mp4" || suffix === ".avi" || suffix === ".flv" || suffix === ".rmvb")
                return "images/video.svg";
            else if (suffix === ".doc" || suffix === ".docx")
                return "images/word.svg";
            else if (suffix === ".xml")
                return "images/xml.svg";
            else if (suffix === ".zip" || suffix === ".rar" || suffix === ".war" || suffix === ".7z")
                return "images/zip.svg";
            else if (suffix === ".sql")
                return "images/sql.svg";
            else if (suffix === ".db")
                return "images/database.svg";
            else if (suffix === ".java")
                return "images/java.svg";
            else if (suffix === ".py")
                return "images/python.svg";
            else if (suffix === ".php")
                return "images/php.svg";
            else if (suffix === ".json")
                return "images/json.svg";
            else if (suffix === ".css")
                return "images/css.svg";
            else if (suffix === ".js")
                return "images/js.svg";
            else if (suffix === ".html")
                return "images/html.svg";
            else if (suffix === ".jar")
                return "images/jar.svg";
            else if (suffix === ".apk")
                return "images/apk.svg";
            else if (suffix === ".desktop")
                return "images/desktop.svg";
            else if (suffix === ".lnk" || suffix === ".url")
                return "images/link.svg";
            else if (suffix === ".rpm")
                return "images/rpm.svg";
            else if (suffix === ".iso")
                return "images/iso.svg";
            else if (suffix === ".dll")
                return "images/dll.svg";
            else if (suffix === ".bak")
                return "images/bak.svg";
            else
                return "images/unknown.svg";
        }
        // 格式化时间
        window.dateFormat = function(fmt, date) {
            var ret;
            const opt = {
                "y+": date.getFullYear().toString(),        // 年
                "M+": (date.getMonth() + 1).toString(),     // 月
                "d+": date.getDate().toString(),            // 日
                "H+": date.getHours().toString(),           // 时
                "m+": date.getMinutes().toString(),         // 分
                "s+": date.getSeconds().toString()          // 秒
                // 有其他格式化字符需求可以继续添加，必须转化成字符串
            };
            for (var k in opt) {
                ret = new RegExp("(" + k + ")").exec(fmt);
                if (ret) {
                    fmt = fmt.replace(ret[1], (ret[1].length == 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, "0")))
                };
            };
            return fmt;
        };

        // 登出
        logout("#logout");
    });
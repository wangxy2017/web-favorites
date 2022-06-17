 layui.use(['layer', 'form', 'laydate', 'flow'], function() {
        var layer = layui.layer;
        var form = layui.form;
        var laydate = layui.laydate;
        var flow = layui.flow;

        laydate.render({
            elem: '#calendarInput',
            type: 'month',
            format: 'yyyy年MM月',
            value: new Date(),
            min: '1970-01-01',
            max: '2970-12-31',
            btns: ['now','confirm'],
            done: function(value, date, endDate) {
                var text = value.substring(0, 4) + '-' + value.substring(5, 7);
                drawCalender(text);
            }
        });

        // 获取某月第一天是星期几
        window.getFirstDayWeekDay = function(text) { // 2021-12
            var split = text.split('-');
            var d = new Date(parseInt(split[0]), parseInt(split[1]) - 1, 1);
            d.setDate(1);
            return d.getDay();
        };

        // 获取某月有多少天
        window.getCurrMonthDays = function(text) { // 2021-12
            var split = text.split('-');
            var d = new Date(parseInt(split[0]), parseInt(split[1]), 0);
            return d.getDate();
        };

        // 获取上个月有多少天
        window.getPreMonthDays = function(text) { // 2021-12
            var split = text.split('-');
            var d = new Date(parseInt(split[0]), parseInt(split[1]) - 1, 1);
            if (d.getMonth() == 0) {
                d.setFullYear(d.getFullYear() - 1);
                d.setMonth(11);
            } else {
                d.setMonth(d.getMonth() - 1);
            }
            return new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();
        };

        // 获取上一个月
        window.getPreMonth = function(text) { // 2021-12
            var split = text.split('-');
            var d = new Date(parseInt(split[0]), parseInt(split[1]) - 1, 1);
            if (d.getMonth() == 0) {
                d.setFullYear(d.getFullYear() - 1);
                d.setMonth(11);
            } else {
                d.setMonth(d.getMonth() - 1);
            }
            return d.getFullYear() + '-' + (d.getMonth() + 1 < 10 ? '0' : '') + (d.getMonth() + 1);
        };

        // 获取下一个月
        window.getNextMonth = function(text) { // 2021-12
            var split = text.split('-');
            var d = new Date(parseInt(split[0]), parseInt(split[1]) - 1, 1);
            if (d.getMonth() == 11) {
                d.setFullYear(d.getFullYear() + 1);
                d.setMonth(0);
            } else {
                d.setMonth(d.getMonth() + 1);
            }
            return d.getFullYear() + '-' + (d.getMonth() + 1 < 10 ? '0' : '') + (d.getMonth() + 1);
        };

        // 获取今天日期
        window.getToday = function() {
            var now = new Date();
            return now.getFullYear() + '-' + (now.getMonth() + 1 < 10 ? '0' : '') + (now.getMonth() + 1) + '-' + (now.getDate() <
                10 ?
                '0' :
                '') + now.getDate()
        };

        // 获取农历
        window.getLunar = function(text) { // 2020-01-11
            var split = text.split("-");
            var lunar = calendar.solar2lunar(split[0], split[1], split[2]);
            return lunar;
        };

        window.isHoliday = function(lunar) {
            var cArr = ['1-1','1-2','1-3','4-5','5-1','5-2','5-3','10-1','10-2','10-3','10-4','10-5','10-6','10-7'];
            var lArr = ['12-30','1-1','1-2','1-3','1-4','1-5','1-6','5-5','8-15'];
            return cArr.indexOf(lunar.cMonth + '-' + lunar.cDay) > -1 || lArr.indexOf(lunar.lMonth + '-' + lunar.lDay) > -1;
        };

        // 日期比较
        window.dateCompare = function(text1,text2){ // 2020-01-11
            var time1 = new Date(text1.replace(/-/g, "/")).getTime();
            var time2 = new Date(text2.replace(/-/g, "/")).getTime();
            return time1 - time2;
        }

        window.getLabel = function(level){
            var html = '';
            switch(level) {
                case 0:
                    html = '<span class="layui-badge task-level">紧急</span>';
                    break;
                case 1:
                    html = '<span class="layui-badge layui-bg-orange task-level">优先</span>';
                    break;
                case 2:
                    html = '<span class="layui-badge layui-bg-green task-level">重要</span>';
                    break;
                case 3:
                    html = '<span class="layui-badge layui-bg-blue task-level">待办</span>';
                    break;
                case 4:
                    html = '<span class="layui-badge layui-bg-gray task-level">完成</span>';
                    break;
                case 5:
                    html = '<span class="layui-badge-rim task-level">取消</span>';
                    break;
                default:
            }
            return html;
        };

        // 加载数据
        window.loadDataList = function(text){// 2020-09
            layer.load();
            $.ajax({
                type: "GET",
                url: "task/all/" + text,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    if (result.code == 0) {
                        var today = getToday();// 获取今天日期，与任务日期比较，判断是历史任务还是未来任务。
                        $(result.data).each(function(index,item){
                            var html = '';
                            var date = item.date;
                            if(dateCompare(date,today) >= 0){// 未来任务，显示任务个数
                                html += '<div class="task-count">';
                                if(item.blueTasks && item.blueTasks > 0){
                                    html += '<span class="layui-badge layui-bg-blue">' + item.blueTasks + '</span>';
                                }
                                if(item.greenTasks && item.greenTasks > 0){
                                    html += '<span class="layui-badge layui-bg-green">' + item.greenTasks + '</span>';
                                }
                                if(item.orangeTasks && item.orangeTasks > 0){
                                    html += '<span class="layui-badge layui-bg-orange">' + item.orangeTasks + '</span>';
                                }
                                if(item.redTasks && item.redTasks > 0){
                                    html += '<span class="layui-badge layui-bg-red">' + item.redTasks + '</span>';
                                }
                                if(item.grayTasks && item.grayTasks > 0){
                                    html += '<span class="layui-badge layui-bg-gray">' + item.grayTasks + '</span>';
                                }
                                if(item.cancelTasks && item.cancelTasks > 0){
                                    html += '<span class="layui-badge-rim">' + item.cancelTasks + '</span>';
                                }
                                html += '</div>';
                            }else{// 历史任务，显示历史图标
                                html += '<span class="layui-badge layui-bg-gray task-history">历史</span>';
                            }
                            var day = $("#" + date).append(html);
                            dayHover(day,date,today,item);
                            alarmHover(day);
                        });
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
        };

        // 画日历
        window.drawCalender = function(text) { // 2021-12
            if (!text) {
                var now = new Date();
                text = now.getFullYear() + '-' + (now.getMonth() + 1 < 10 ? '0' : '') + (now.getMonth() + 1);
            }
            // 获取当前月份第一天星期几
            var firstDayWeekDay = getFirstDayWeekDay(text);
            // 获取当前月份有多少天
            var currMonthDays = getCurrMonthDays(text);
            // 获取上月有多少天
            var preMonthDays = getPreMonthDays(text);
            var currMonthCount = 1;
            var currMonthBlock = firstDayWeekDay + currMonthDays - 1;
            var nextMonthCount = 1;
            var currMonth = text;
            var preMonth = getPreMonth(text);
            var nextMonth = getNextMonth(text);
            var today = getToday();
            // 画格子
            var html = '';
            html += '<div class="week-list">';
            html += '	<div class="week-item">星期日</div>';
            html += '	<div class="week-item">星期一</div>';
            html += '	<div class="week-item">星期二</div>';
            html += '	<div class="week-item">星期三</div>';
            html += '	<div class="week-item">星期四</div>';
            html += '	<div class="week-item">星期五</div>';
            html += '	<div class="week-item">星期六</div>';
            html += '</div>';
            for (var i = 0; i < 6; i++) {
                html += '<div class="day-list">';
                for (var j = 0; j < 7; j++) {
                    html += '	<div class="day-item">';
                    html += '		<span class="date"></span>';
                    html += '		<span class="lunar"></span>';
                    html += '	</div>';
                }
                html += '</div>';
            }
            $("#calendar").addClass("calendar-table").empty().append(html);
            // 填数据
            var nowDay = parseInt(today.substring(8));
            var nowMonth = today.substring(0,7);
            $('#calendar .day-item').each(function(index) {
                var day;
                if (index < firstDayWeekDay) {
                    var num = preMonthDays - (firstDayWeekDay - index - 1);
                    day = preMonth + '-' + (num < 10 ? '0' : '') + num;
                    $(this).attr("id", day).addClass('last-month').find('.date').text(num);
                } else if (index >= firstDayWeekDay && index <= currMonthBlock) {
                    day = currMonth + '-' + (currMonthCount < 10 ? '0' : '') + currMonthCount;
                    // 过去 现在 未来
                    var c = '';
                    var value = dateCompare(day,today);
                    if(value < 0){
                        c = 'day-before';
                    }else if(value === 0){
                        c = 'day-today';
                    }else {
                        c = 'day-after';
                    }
                    $(this).attr("id", day).addClass(c).find('.date').text(currMonthCount);
                    currMonthCount++;
                } else {
                    day = nextMonth + '-' + (nextMonthCount < 10 ? '0' : '') + nextMonthCount;
                    $(this).attr("id", day).addClass('next-month').find('.date').text(nextMonthCount);
                    nextMonthCount++;
                }
                // 农历
                var lunar = getLunar(day);
                $(this).find(".lunar").text(lunar.IDayCn === "初一" ? lunar.IMonthCn : lunar.IDayCn);
                // 节假日
                if(isHoliday(lunar)) $(this).append('<span class="holiday">假</span>');
            });
            // 给今天和明天添加新增日程按钮
            $(".day-item.day-after,.day-item.day-today").each(function(){
                var d = $(this).attr("id");
                var html = '';
                html += '<div class="layui-anim layui-anim-fadein task-new-mask">';
                html += '	<button type="button" class="layui-btn layui-btn-sm" onclick="addTask(\''+ d +'\')">新增日程</button>';
                html += '</div>';
                $(this).append(html);
            });
            // 请求后端数据
            loadDataList(text);
        };

        window.dayHover = function(day,date,today,item){
            // 监听日历hover事件，初始化任务面板
            day.hover(function(){
                var taskView = $(this).find('.task-view');
                if(!taskView[0]){
                    // 创建任务面板
                    var html = '';
                    html += '<div class="layui-anim layui-anim-fadein task-view">';
                    html += '   <div class="task-header">';
                    html += '	    <span>' + date + '</span>';
                    html += '	</div>';
                    html += '	<div class="task-list" id="task' + date + '">';
                    html += '   </div>';
                    html += '   <div class="layui-btn-container">';
                    if(dateCompare(date,today) >= 0){
                        html += '   <button type="button" class="layui-btn layui-btn-sm" onclick="addTask(\'' + item.date + '\')">新增日程</button>';
                    }else{
                        html += '   <button type="button" class="layui-btn layui-btn-normal layui-btn-sm" onclick="cleanTask(\'' + item.date + '\')">清除历史</button>';
                    }
                    html += '   </div>';
                    html += '</div>';
                    taskView = $(this).append(html).find('.task-view');
                    // 初始化数据
                    $('#task' + date).unbind();
                    flow.load({
                        elem: '#task' + date
                        ,scrollElem: '#task' + date
                        ,mb: 50
                        ,end: ' '
                        ,done: function(page, next){
                          var lis = [];
                          $.ajax({
                            type: "GET",
                            url: "task/list",
                            data: {"date": date, "pageNum": page,"pageSize": 20},
                            dataType: "json",
                            headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                            success: function (result) {
                                if (result.code == 0) {
                                    $(result.data.list).each(function(i,t){
                                        var html = '';
                                        html += '   <div class="task-item">';
                                        html += getLabel(t.level);
                                        html += '	    <span class="content">' + t.content + '</span>';
                                        if(t.level < 4){
                                            html += '   <div class="layui-btn-container task-action" data-id="' + t.id + '">';
                                            if(t.isAlarm && alarmTimeEffect(t.alarmTime)){
                                                html += '   <img class="alarm-icon" src="images/alarm.svg" data-time="' + t.alarmTime.substring(11) + '">';
                                            }
                                            html += '       <button type="button" class="layui-btn layui-btn-xs layui-btn-normal" onclick="doTask(this)">完成</button> ';
                                            if(dateCompare(date,today) >= 0){
                                                html += '   <button type="button" class="layui-btn layui-btn-xs layui-btn-danger" onclick="deleteTask(this)">删除</button> ';
                                            }else{
                                                html += '   <button type="button" class="layui-btn layui-btn-xs layui-btn-primary" onclick="cancelTask(this)">取消</button> ';
                                            }
                                            html += '   </div>';
                                        }
                                        html += '   </div>';
                                        lis.push(html);
                                    });
                                    next(lis.join(''), page < result.data.pages);
                                    // 数据加载完成
                                    calcPosition(taskView);
                                }
                            }
                          });
                        }
                    });
                }else{
                    calcPosition(taskView);
                }
            },function(){
                $(this).find('.task-view').hide();
            }).find(".task-new-mask").remove();// 移除添加任务按钮：如果是历史时间，不需要按钮。如果是未来时间且有任务，则在任务面板添加。所以这里不需要。
        };

        window.alarmHover = function(day){
            // 监听闹钟hover事件
            var tipIndex;
            day.on('mouseenter','.alarm-icon',function(e){
                var that = $(e.currentTarget);
                tipIndex = layer.tips(that.attr("data-time"), that, {tips: 4});
            }).on('mouseleave','.alarm-icon',function(e){
                layer.close(tipIndex);
            });
        };

        window.calcPosition = function(taskView){
            var that = taskView.parent();
            // 初始化面板后，计算面板显示位置
            var tWidth = parseInt(taskView.width());
            var tHeight = parseInt(taskView.height());
            var width = parseInt(that.width());
            var height = parseInt(that.height());
            var cWidth = parseInt($("#calendar").width());
            var cHeight = parseInt($("#calendar").height());
            var cTop = $("#calendar").offset().top;
            var cLeft = $("#calendar").offset().left;
            var top = that.offset().top - cTop;
            var left = that.offset().left - cLeft;
            var y_over = top + height + tHeight - cHeight;
            var x_over = left + tWidth - cWidth;
            // 相对于原点，计算移动距离
            if(y_over > 0){
                var min = 0 - y_over;
                var max = 0 - (height + tHeight);
                var real_max = 0 - (height + top);
                var final = min - Math.abs(min - Math.max(max,real_max)) / 2;
                taskView.css("top", height + parseInt(final));
            }
            if(x_over > 0){
                var min = 0 - x_over;
                var max = 0 - Math.abs(width - tWidth);
                var real_max = 0 - left;
                var final = min - Math.abs(min - Math.max(max,real_max)) - 20;
                taskView.css("left", parseInt(final));
            }
            taskView.show();
        };

        // 初始化日历
        drawCalender();

        // 邮件提醒开关
        form.on('switch(isAlarm)', function(data){
            if(data.elem.checked){
                $("#alarmTime").attr("lay-verify","required");
            }else{
                $("#alarmTime").removeAttr("lay-verify");
            }
        });

        form.verify({
          length: function (value, item) {
            if(value.length>500){
                return '最多输入500个字符';
            }
          }
        });

        $("#leftBtn").click(function() {
            var date = $("#calendarInput").val();
            var preMonth = getPreMonth(date.substring(0, 4) + '-' + date.substring(5, 7));
            var split = preMonth.split("-");
            if(split[0] < 1970){
                return false;
            }
            $("#calendarInput").val(split[0] + "年" + split[1] + "月");
            drawCalender(preMonth);
        });

        window.alarmTimeEffect = function(time){
            return new Date(time.replace(/-/g, "/")).getTime() > new Date().getTime();
        };

        $("#rightBtn").click(function() {
            var date = $("#calendarInput").val();
            var nextMonth = getNextMonth(date.substring(0, 4) + '-' + date.substring(5, 7));
            var split = nextMonth.split("-");
            if(split[0]> 2970){
                return false;
            }
            $("#calendarInput").val(split[0] + "年" + split[1] + "月");
            drawCalender(nextMonth);
        });

        $("#levelTips").hover(function(){
            var html = '';
            html +='事件等级：<br>';
            html +='1.紧急：紧急且重要<br>';
            html +='2.优先：紧急但不重要<br>';
            html +='3.重要：不紧急但重要<br>';
            html +='4.待办：不紧急也不重要<br>';
            layer.tips(html, this, {tips: 3, time: 0});
        },function(){
            layer.closeAll('tips');
        });

        form.on('select(alarmTime)', function(data){
          if(data.value && !$("#isAlarm").is(':checked')){
            $("#isAlarm").prop("checked", true);
            form.render('checkbox', 'taskForm');
          }
        });

        // 监听提交
        form.on('submit(saveTaskBtn)', function(data) {
            var postData = data.field;
            if(postData.isAlarm && postData.alarmTime){
                postData.alarmTime = postData.taskDate + ' ' + postData.alarmTime;
            }else{
                postData.alarmTime = "";
            }
            layer.load();
            $.ajax({
                type: "POST",
                url: "task",
                data: JSON.stringify(postData),
                contentType: 'application/json;charset=utf-8',
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    layer.closeAll('loading');
                    layer.close(indexMap.get('#taskForm'));
                    if (result.code == 0) {
                        window.location.reload();
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
            return false;
        });

        // 新增日程点击事件
        window.addTask = function(text){// 2020-01-01
            $('#taskForm')[0].reset();
            $("#taskDate").val(text);
            var index = layer.open({
                type: 1,
                skin: 'to-fix-select',
                content: $('#taskForm')
            });
            indexMap.set('#taskForm',index);
            // 禁用部分时间选项
            $.each($("#alarmTime").children(),function(i,item){
                var that = $(item);
                var time = text + ' ' + that.attr("value");
                if(new Date(time.replace(/-/g, "/")).getTime() <= new Date().getTime()){
                    that.attr("disabled", true);
                }else{
                    that.removeAttr("disabled");
                }
            });
            form.render('select','taskForm');
        };

        // 清除任务
        window.cleanTask = function(text){
            layer.confirm('确认清除历史任务吗？', function(index){
                layer.close(index);

                 $.ajax({
                    type: "GET",
                    url: "task/clean/" + text ,
                    dataType: "json",
                    headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                    success: function (result) {
                        if (result.code == 0) {
                            layer.msg('操作成功', {icon: 6});
                            $("#task"+text).empty();
                        } else {
                            layer.msg(result.msg, {icon: 5});
                        }
                    }
                });
            });
        };

        // 完成任务
        window.doTask = function(obj){
            var id = $(obj).parent().attr("data-id");
            $.ajax({
                type: "GET",
                url: "task/done/" + id ,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        var task_item = $(obj).parent().parent();
                        layer.msg('操作成功', {icon: 6});
                        task_item.find('.task-level').replaceWith('<span class="layui-badge layui-bg-gray task-level">完成</span>');
                        task_item.find('.task-action').remove();
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
        };

        // 取消任务
        window.cancelTask = function(obj){
            var id = $(obj).parent().attr("data-id");
            $.ajax({
                type: "GET",
                url: "task/cancel/" + id ,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        var task_item = $(obj).parent().parent();
                        layer.msg('操作成功', {icon: 6});
                        task_item.find('.task-level').replaceWith('<span class="layui-badge-rim task-level">取消</span>');
                        task_item.find('.task-action').remove();
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
        };

        // 删除任务
        window.deleteTask = function(obj){
            var id = $(obj).parent().attr("data-id");
            $.ajax({
                type: "GET",
                url: "task/delete/" + id ,
                dataType: "json",
                headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
                success: function (result) {
                    if (result.code == 0) {
                        layer.msg('删除成功', {icon: 6});
                        $(obj).parent().parent().remove();
                    } else {
                        layer.msg(result.msg, {icon: 5});
                    }
                }
            });
        };

        $("#cancelSaveTaskBtn").click(function(){
            layer.close(indexMap.get('#taskForm'));
        });

        // 登出
        logout("#logout");

    });
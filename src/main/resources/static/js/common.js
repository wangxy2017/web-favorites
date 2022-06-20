// jQuery全局拦截器
$(document).ajaxSuccess(function(event,xhr,options){
    var result = xhr.responseJSON;
    if(result != undefined && (result.code == 401 || result.code == 403) && window.location.href.indexOf("/login.html") == -1){
        window.location.href= "login.html";
    }
});

// 全局变量
var windowWidth = parseInt($(window).width()) * 0.9;// 窗口宽度
var indexMap = new Map();// 弹出层索引容器
var debug = false // 调试模式

//数组扩展contains适用于数组判断
Array.prototype.contains = function(a) {
  if ("string" == typeof a || "number" == typeof a) for (var b in this) if (a == this[b]) return ! 0;
  return ! 1
};

// 根据数组的下标，删除该下标的元素
Array.prototype.remove = function(val) {
    var index = this.indexOf(val);
    if (index > -1) {
        this.splice(index, 1);
    }
};

// 添加时间后缀参数
window.timeSuffix = function(){
    return debug ? "_t=" + new Date().getTime() : "";
};

// 绑定搜索快捷键
window.initSearch = function(id){
    $(document).on("keydown", function(event){
        if(event.ctrlKey && event.key === "f"){
            $(id).focus();
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
};

// 退出登录
window.logout = function(id){
    $(id).click(function () {
        layer.confirm('确认退出系统吗？', function(index){
            layer.close(index);
            localStorage.clear();
            window.location.href = "login.html";
        });
    });
};
// 打开新窗口
window.newWin = function(url) {
  var a = document.createElement('a');
  a.setAttribute('href', url);
  a.setAttribute('target', '_blank');
  document.body.appendChild(a);
  a.click();
  a.remove();
};
// 判断移动端
window.isMobile = function(){
    let info = navigator.userAgent;
    let agents = ["Android", "iPhone", "SymbianOS", "Windows Phone", "iPod", "iPad"];
    for(let i = 0; i < agents.length; i++){
        if(info.indexOf(agents[i]) >= 0) return true;
    }
    return false;
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
// 格式化数字
window.transform = function(value) {
    if(value == null || isNaN(value)){
        return 0;
    }
    let newValue = ['', '', ''];
    let fr = 1000;
    const ad = 1;
    let num = 3;
    const fm = 1;
    while (value / fr >= 1) {
      fr *= 10;
      num += 1;
    }
    if (num <= 4) { // 千
      newValue[1] = '千';
      newValue[0] = parseInt(value / 1000) + '';
    } else if (num <= 8) { // 万
      const text1 = parseInt(num - 4) / 3 > 1 ? '千万' : '万';
      // tslint:disable-next-line:no-shadowed-variable
      const fm = '万' === text1 ? 10000 : 10000000;
      newValue[1] = text1;
      newValue[0] = (value / fm) + '';
    } else if (num <= 16) {// 亿
      let text1 = (num - 8) / 3 > 1 ? '千亿' : '亿';
      text1 = (num - 8) / 4 > 1 ? '万亿' : text1;
      text1 = (num - 8) / 7 > 1 ? '千万亿' : text1;
      // tslint:disable-next-line:no-shadowed-variable
      let fm = 1;
      if ('亿' === text1) {
        fm = 100000000;
      } else if ('千亿' === text1) {
        fm = 100000000000;
      } else if ('万亿' === text1) {
        fm = 1000000000000;
      } else if ('千万亿' === text1) {
        fm = 1000000000000000;
      }
      newValue[1] = text1;
      newValue[0] = parseInt(value / fm) + '';
    }
    if (value < 1000) {
      newValue[1] = '';
      newValue[0] = value + '';
    }
    return newValue.join('');
};

// 计算最近
window.todo_time = function(time) {
    if (!time) {
      return '';
    }
    var oDate = new Date();
    var newHaoMiao1 = oDate.getTime(); // 当前时间,含有时分秒
    oDate.setHours(0);
    oDate.setMinutes(0);
    oDate.setSeconds(0);
    oDate.setMilliseconds(0);
    var newHaoMiao2 = oDate.getTime(); // 当前时间,不含有时分秒
    var newTime = time.replace(new RegExp("-", "gm"), "/");
    var arrTime = time.substring(0, 11).replace(new RegExp("-", "gm"), "/"); // 截取时间，不含有时分秒
    var showTime = time.substring(0, 11);
    var oldHaoMiao1 = new Date(newTime).getTime(); // 含有时分秒的转化成毫秒
    var oldHaoMiao2 = new Date(arrTime).getTime(); // 不含有时分秒的转化成毫秒
    var d1 = (newHaoMiao1 - oldHaoMiao1) / 1000;
    var d2 = (newHaoMiao2 - oldHaoMiao2) / 1000;
    var d_result = '';
    if (d2 > 0) { // 是几天前
      var d_days = parseInt(d2 / 86400);
      if (d_days === 1) {
        d_result = "昨天";
      } else if (d_days >= 2) {
        d_result = showTime;
      }
    } else { // 是今天
      var d_hours = parseInt(d1 / 3600);
      var d_minutes = parseInt(d1 / 60);
      if (d_hours > 0) {
        d_result = d_hours + "小时前";
      } else if (d_hours <= 0 && d_minutes > 0) {
        d_result = d_minutes + "分钟前";
      } else {
        d_result = "刚刚";
      }
    }
    return d_result;
};
// html反转义
window.escapeHtml = function(str){
     return str.replace(/[<>&\s\n"]/ig,function(c){return {'<':'&lt;','>':'&gt;','&':'&amp;','"':'&quot;',' ':'&nbsp;','\n':'<br/>'}[c];});
};
// html转义
window.htmlEscape = function(html){
     return html.replace(/&lt;|&gt;|&amp;|&quot;|&nbsp;|<br\/>/ig,function(c){return {'&lt;':'<','&gt;':'>','&amp;':'&','&quot;':'"','&nbsp;':' ','<br/>':'\n'}[c];});
};
// 下载文件
window.downloadFile = function(filename, url){
    // 此处不加载动画，只关闭动画，加载动画交给前面的函数执行
    $.ajax({
        url: url,
        headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
        xhrFields: { responseType: "arraybuffer" },
        success: function(result){
            layer.closeAll('loading');
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
            layer.closeAll('loading');
            layer.msg(result.msg, {icon: 5});
        }
    });
};
// 代码高亮
window.wrapSearch = function(content,keyword){
    var text = '<span class="highLight">'+keyword+'</span>';
    var reg = new RegExp(keyword,"gi");
    return content.replace(reg,text);
};
// 取消高亮
window.unwrapSearch = function(content,keyword){
    var text = '<span class="highLight">'+keyword+'</span>';
    var reg = new RegExp(text,"gi");
    return content.replace(reg,keyword);
};

// 初始化title
$(document).on('mouseenter','[lay-title]',function(e){
    var that = $(e.currentTarget);
    layer.tips(that.attr("lay-title"), that, {tips: 3, time: 0});
}).on('mouseleave','[lay-title]',function(e){
    layer.closeAll('tips');
});
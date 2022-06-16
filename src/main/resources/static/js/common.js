// jQuery全局拦截器
$(document).ajaxSuccess(function(event,xhr,options){
    var result = xhr.responseJSON;
    if(result != undefined && (result.code == 401 || result.code == 403) && window.location.href.indexOf("login.html") == -1){
        window.location.href= "login.html";
    }
});

// 全局变量
var windowWidth = parseInt($(window).width()) * 0.9;// 窗口宽度
var indexMap = new Map();// 弹出层索引容器
var debug = true // 调试模式

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
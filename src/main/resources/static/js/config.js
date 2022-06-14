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
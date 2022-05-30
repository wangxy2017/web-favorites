// jQuery全局拦截器
$(document).ajaxSuccess(function(event,xhr,options){
    var result = xhr.responseJSON;
    if(result != undefined && result.code == "401"){
        window.location.href= "login.html";
    }
});
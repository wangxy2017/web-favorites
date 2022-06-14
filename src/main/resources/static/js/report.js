layui.use(['element'], function() {
       var element = layui.element;

       $.ajax({
            type: "GET",
            url: "user/data",
            dataType: "json",
            headers:{"Authorization": "Bearer "+ localStorage.getItem("login_user_token")},
            success: function (result) {
                if (result.code == 0) {
                    var data = result.data;
                    $("#clickCount").numberRock({speed:1,count:data.clickCount});
                    $("#searchCount").numberRock({speed:1,count:data.searchCount});
                    $("#registerDay").numberRock({speed:1,count:data.registerDay});
                    $("#onlineHour").numberRock({speed:1,count:data.onlineHour});
                    $("#categoryCount").numberRock({speed:1,count:data.categoryCount});
                    $("#favoriteCount").numberRock({speed:1,count:data.favoriteCount});
                    $("#momentCount").numberRock({speed:1,count:data.momentCount});
                    $("#taskCount").numberRock({speed:1,count:data.taskCount});
                    $("#navigationCount").numberRock({speed:1,count:data.navigationCount});
                    $("#memorandumCount").numberRock({speed:1,count:data.memorandumCount});
                    $("#searchTypeCount").numberRock({speed:1,count:data.searchTypeCount});
                    $("#fileCount").numberRock({speed:1,count:data.fileCount});
                    $("#shareCount").numberRock({speed:1,count:data.shareCount});
                    $("#recycleCount").numberRock({speed:1,count:data.recycleCount});
                }
            }
        });
    });
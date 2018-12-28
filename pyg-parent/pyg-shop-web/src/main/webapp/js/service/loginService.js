app.service("loginService",function ($http) {
    //读取登录用户名
    this.loginName = function () {
        
        return $http.get("../login/getLoginName.do");
    }
});
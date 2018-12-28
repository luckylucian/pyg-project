 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){
	$scope.reg = function () {
        //比较两次输入的密码是否一致
        if ($scope.entity.password != $scope.password){
            alert("两次输入的密码不一致，请重新输入");
            return;
        }

        //新增注册用户
        userService.add($scope.entity,$scope.smscode).success(
            function (response) {
                alert(response.message);
            }
        );
    }

    $scope.sendCode=function () {
        //判断手机号是否为空
        if ($scope.entity.phone == null || $scope.entity.phone == ""){
            alert("请输入手机号！");
            return;
        }

        userService.sendCode($scope.entity.phone).success(
            function (response) {
                alert(response.message);
            }
        );
    }
    
});	

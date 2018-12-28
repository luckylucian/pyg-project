app.controller("seckillGoodsController", function ($scope, $interval, $location, seckillGoodsService) {

    $scope.findList = function () {

        seckillGoodsService.findList().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    $scope.findOne = function () {

        seckillGoodsService.findOne($location.search()['id']).success(
            function (response) {
                $scope.entity = response;

                totalSecond = Math.floor((new Date($scope.entity.endTime).getTime() - new Date().getTime()) / 1000);    //总秒数
                time = $interval(function () {
                    totalSecond = totalSecond - 1;  //totalSecond不需要在前台展示，所以不加$scope
                    $scope.timeString = convertTimeString(totalSecond);

                    alert(timeString);

                    if (totalSecond <= 0) {
                        $interval.cancel(time);
                    }
                }, 1000)
            }
        );
    }

    //将总秒数转换为   天 - 小时 - 分 - 秒  的字符串格式
    convertTimeString = function (totalSecond) {

        var days = Math.floor(totalSecond / (60 * 60 * 24));  //天数
        var hours = Math.floor((totalSecond - days * 24 * 60 * 60) / (60 * 60));  //小时数
        var minutes = Math.floor((totalSecond - days * 24 * 60 * 60 - hours * 60 * 60) / 60);    //分钟数
        var seconds = totalSecond - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60;    //秒数

        var timeString = "";
        if (days > 0) {
            timeString = days + "天";
        }
        return timeString + hours + ":" + minutes + ":" + seconds;
    }

    //提交订单
    $scope.submitOrder=function(){
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function(response){
                if(response.success){
                    alert("下单成功，请在 1 分钟内完成支付");
                    location.href="pay.html";
                }else{
                    alert(response.message);
                }
            }
        );
    }

});
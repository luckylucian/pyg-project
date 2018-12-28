app.controller("baseController", function ($scope) {
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();
        }
    };

    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);//重新加载
    }


    $scope.selectIds = [];  //用户勾选的 id 集合

    //用户勾选复选框
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);  // push 向集合中添加元素
        } else {
            var index = $scope.selectIds.indexOf(id);   //查找值的位置
            $scope.selectIds.splice(index, 1);   //移除的位置  移除的个数
        }
    }

    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ",";
            }
            value += json[i][key];
        }
        return value;
    }


    //从集合中根据 key 查询对象   （通用的）
    //参数 list 集合  ， key 为想要查询的集合的key ，keyValue是现有的规格名称
    $scope.searchObjectByKey =function (list,key,keyValue) {
        //遍历list集合
        for(var i = 0 ; i < list.length ; i++ ){
            if(list[i][key] == keyValue){
                return list[i];
            }
        }
        return null;
    }

});
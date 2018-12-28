app.controller("brandController", function ($scope,$controller, brandService) {

    $controller('baseController',{$scope:$scope});  //伪继承

    //查询品牌列表
    $scope.findAll = function () {
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页
    $scope.findByPage = function (page, size) {
        brandService.findByPage(page, size).success(
            function (response) {
                $scope.listByPage = response.rows;  //显示当前页数据
                $scope.paginationConf.totalItems = response.total;  //更新总记录数
            }
        );
    }

    //新增
    $scope.save = function () {
        var object = null;   //方法名称

        if ($scope.newEntity.id != null) {
            object = brandService.update($scope.newEntity);
        }else{
            object = brandService.add($scope.newEntity);
        }

        //如果是post请求，参数就不能只有url ，还需要请求体
        object.success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            }
        );
    }

    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.newEntity = response;    //response 中的数据直接通过双向绑定给 newEntity中的属性
            }
        );
    }

    $scope.del =function () {
        brandService.del($scope.selectIds).success(
            function (response) {
                if (response.success){
                    $scope.reloadList();
                }else{
                    alert(response.message);

                }
            }
        )
    }

    $scope.searchEntity = {};
    //条件查询
    $scope.search = function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        )
    }
});
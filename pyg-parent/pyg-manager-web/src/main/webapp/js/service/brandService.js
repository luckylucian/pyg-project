app.service('brandService',function ($http) {
    this.findAll = function () {
        return $http.get("../brand/findAll.do");
    }

    this.findByPage = function (page,size) {
        return $http.get("../brand/findByPage.do?page= " + page + "&size=" + size);
    }

    this.findOne = function (id) {
        return $http.get("../brand/findOne.do?id=" + id);
    }

    this.add = function (newEntity) {
        return $http.post("../brand/add.do", newEntity);
    }

    this.update = function (newEntity) {
        return $http.post("../brand/update.do", newEntity);
    }

    this.del = function (selectIds) {
        return $http.get("../brand/delete.do?ids="+ selectIds);
    }

    this.search = function (page,size,searchEntity) {
        return $http.post("../brand/search.do?page="+ page + "&size=" + size,searchEntity);
    }

    this.selectOptionList=function () {
        return $http.get('../brand/selectOptionList.do');
    }
})
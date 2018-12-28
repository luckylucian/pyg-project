package com.pyg.service;

import com.pyg.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;


public interface BrandService {

    /**
     * 查询所有brand
     * @return
     */
    public List<TbBrand> findAll();


    /**
     * 品牌分页
     * @param pageNum  当前页码
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findByPage(int pageNum, int pageSize);

    /**
     * 增加
     * @param brand
     */
    public void add(TbBrand brand);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public TbBrand findOne(Long id);

    /**
     * 更新brand
     * @param brand
     */
    public void update(TbBrand brand);

    /**
     * 删除选中
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 品牌分页
     * @param brand
     * @param pageNum
     * @param pageSize
     * @return
     */                             //将模糊条件都封装到实体类 brand 中
    public PageResult findByPageAndCondition(TbBrand brand,int pageNum ,int pageSize);

    /**
     * 返回下拉列表数据
     * @return
     */
    public List<Map> selectOptionList();

}

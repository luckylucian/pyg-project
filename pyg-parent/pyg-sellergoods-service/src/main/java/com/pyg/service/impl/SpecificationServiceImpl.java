package com.pyg.service.impl;
import java.util.List;
import java.util.Map;

import com.pyg.mapper.TbSpecificationOptionMapper;
import com.pyg.pojo.TbSpecificationOption;
import com.pyg.pojo.TbSpecificationOptionExample;
import com.pyg.pojoCombine.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSpecificationMapper;
import com.pyg.pojo.TbSpecification;
import com.pyg.pojo.TbSpecificationExample;
import com.pyg.pojo.TbSpecificationExample.Criteria;
import com.pyg.service.SpecificationService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		TbSpecification tbSpecification = specification.getTbSpecification();
		specificationMapper.insert(tbSpecification);

		List<TbSpecificationOption> tbSpecificationOptionList = specification.getTbSpecificationOptionList();
		for (TbSpecificationOption tbSpecificationOption : tbSpecificationOptionList) {
			tbSpecificationOption.setSpecId(tbSpecification.getId());
			specificationOptionMapper.insert(tbSpecificationOption);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//保存修改后的规格
	    specificationMapper.updateByPrimaryKey(specification.getTbSpecification());

	    //删除数据库中原有规格选项
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        //指定规格 ID 为条件
        criteria.andSpecIdEqualTo(specification.getTbSpecification().getId());
        specificationOptionMapper.deleteByExample(example);

        //循环插入修改后的规格选项
        for(TbSpecificationOption tbSpecificationOption : specification.getTbSpecificationOptionList()){
            tbSpecificationOption.setSpecId(specification.getTbSpecification().getId());
            specificationOptionMapper.insert(tbSpecificationOption);
        }

    }
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//根据 规格 id 查询规格
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		//根据 规格 id 条件查询 规格选项
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(id);
		List<TbSpecificationOption> tbSpecificationOptionList = specificationOptionMapper.selectByExample(example);

		//构建组合实体类 返回结果
		Specification specification = new Specification();
		specification.setTbSpecification(tbSpecification);
		specification.setTbSpecificationOptionList(tbSpecificationOptionList);

		return  specification;

	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);

			//同时还要删除关联的 规格选项表中的数据，需要用到条件
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            example.createCriteria().andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(example);

		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	public List<Map> selectOptionList(){
		return specificationMapper.selectOptionList();
	}
	
}

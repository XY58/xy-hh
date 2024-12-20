package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     */
    public void save(CategoryDTO categorydao) {
        Category category = new Category();
        // 对象属性拷贝
        BeanUtils.copyProperties(categorydao, category);
        // 设置默认启用
        category.setStatus(StatusConstant.ENABLE);
        // 设置创建时间
        //category.setCreateTime(LocalDateTime.now());
        // 设置更新时间
        //category.setUpdateTime(LocalDateTime.now());
        // 设置创建人
        //category.setCreateUser(BaseContext.getCurrentId());
        // 设置更新人
        //category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
    }

    /**
     * 分类分页查询
     */
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 开始分页查询
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        // 查询分类返回
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 启用禁用分类
     */
    public void startOrStop(Integer status, Long id) {
        // 构造条件
        Category category = Category.builder()
                .status(status)//启用禁用状态
                .id(id)//分类id
                .build();
        categoryMapper.update(category);
    }

    /**
     * 修改分类
     */
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        // 对象属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);
        // 设置更新人
        //category.setUpdateUser(BaseContext.getCurrentId());
        // 设置更新时间
        //category.setUpdateTime(LocalDateTime.now());
        // 调用mapper层方法
        categoryMapper.update(category);
    }

    /**
     * 删除分类
     */
    public void deleteById(Long id) {
        // 查询菜品数量
        Long count = dishMapper.countByCategory(id);
        if (count > 0) {
            // 抛出业务异常
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        // 查询套餐数量
        count = setmealMapper.countByCategory(id);
        if (count > 0) {
            // 抛出业务异常
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        // 调用mapper层方法
        categoryMapper.deleteById(id);
    }

    /**
     * 根据类型查询分类
     */
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}

package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 新增分类
     */
    @Insert("insert into sky_take_out.category (type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values (#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser});")
    @AutoFill(value = OperationType.INSERT)
    void insert(Category category);

    /**
     * 分类分页查询
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 启用禁用分类
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);

    /**
     * 删除分类
     */
    @Delete("delete from sky_take_out.category where id =#{id}")
    void deleteById(Long id);

    /**
     * 根据类型查询分类
     */
    List<Category> list(Integer type);
}

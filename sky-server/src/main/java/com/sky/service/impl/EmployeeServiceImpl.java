package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.NumConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对密码进行md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     */
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        // 对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置初始密码，123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置状态，1表示启用，0表示禁用
        employee.setStatus(StatusConstant.ENABLE);
        //设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //设置当前记录创建人id和修改人id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //保存员工数据
        employeeMapper.insert(employee);
    }


    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //开始分页
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //查询员工数据
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        //返回结果
        long total = page.getTotal();
        List<Employee> result = page.getResult();
        return new PageResult(total, result);
    }

    /**
     * 启用禁用员工账号
     */
    public void startOrStop(Integer status, Long id) {
        //创建一个Employee对象，设置状态和id
        Employee employee = Employee.builder()
                .status(status)//启用禁用状态
                .id(id)//员工id
                .build();//构建对象
        //调用mapper层方法
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     */
    public Employee getById(Long id) {
        //调用mapper层方法
        Employee employee = employeeMapper.getById(id);
        //设置密码为不可见
        employee.setPassword("******");
        return employee;
    }

    /**
     * 修改员工信息
     */
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //完善修改人信息
        employee.setUpdateTime(LocalDateTime.now());
        //设置修改人id
        employee.setUpdateUser(BaseContext.getCurrentId());
        //调用mapper层方法
        employeeMapper.update(employee);
    }

    /**
     * 修改员工密码
     */
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        //获取当前登录的员工id
        Long currentId = BaseContext.getCurrentId();
        // 根据id查询员工信息
        Employee employee = employeeMapper.getById(currentId);
        //对旧密码进行md5加密
        passwordEditDTO.setOldPassword(DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes()));
        //判断旧密码是否正确
        if (!employee.getPassword().equals(passwordEditDTO.getOldPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        //对新密码进行md5加密
        passwordEditDTO.setNewPassword(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes()));
        //设置新密码
        employee.setPassword(passwordEditDTO.getNewPassword());
        //设置修改人信息
        employee.setId(currentId);
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(employee);
    }
}

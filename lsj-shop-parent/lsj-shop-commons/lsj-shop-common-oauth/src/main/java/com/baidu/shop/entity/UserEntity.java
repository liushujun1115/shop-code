package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @ClassName UserEntity
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Table(name = "tb_user")
@Data
public class UserEntity {

    @Id
    @NotNull(message = "主键不能为空")
    private Integer id;

    private String username;

    private String password;

    private String phone;

    private Date created;

    private String salt;

}

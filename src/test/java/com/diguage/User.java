package com.diguage;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户
 *
 * @author D瓜哥 · https://www.diguage.com
 */
public class User {
    private Integer id;
    private String name;
    private Date birthday;
    private BigDecimal money;

    public User() {
    }

    public User(Integer id, String name, Date birthday, BigDecimal money) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
        this.money = money;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }
}

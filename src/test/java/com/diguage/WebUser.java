package com.diguage;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Web用户
 *
 * @author D瓜哥 · https://www.diguage.com
 */
public class WebUser extends User {
    private String site;

    public WebUser() {
    }

    public WebUser(Integer id, String name, Date birthday, BigDecimal money, String site) {
        super(id, name, birthday, money);
        this.site = site;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}

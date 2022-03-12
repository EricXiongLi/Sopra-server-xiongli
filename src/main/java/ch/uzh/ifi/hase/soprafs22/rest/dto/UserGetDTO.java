package ch.uzh.ifi.hase.soprafs22.rest.dto;

import java.util.Date;

public class UserGetDTO {
    private String username;
    private Date creation_date;
    private Boolean logged_in;
    private Date birthday;
    private Integer id;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    public Boolean getLogged_in() {
        return logged_in;
    }

    public void setLogged_in(Boolean logged_in) {
        this.logged_in = logged_in;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

package emptyvessel.worklist.dto;

import jakarta.validation.constraints.Size;

public class UserUpdateDto {

    private String nickname;

    private String realName;

    private Integer gender;

    private String email;

    private String telephone;

    private Long departmentId;

    @Size(min = 6, message = "密码不少于6位")
    private String password;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String password() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
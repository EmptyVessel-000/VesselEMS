package vesselems.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import vesselems.model.User;

public class RegisterDto {

    @NotBlank
    private String username;

    @NotBlank
    private String nickname;

    @NotBlank
    private String realName;

    private Integer gender;

    @NotBlank
    @Email
    private String email;

    @Pattern(regexp = "^[\\d\\-+]{0,32}$")
    private String telephone;

    @NotBlank
    private String password;

    public RegisterDto() {
    }

    public void apply(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Target user cannot be null");
        }
        user.setUsername(username);
        user.setNickname(nickname);
        user.setRealName(realName);
        user.setGender(gender);
        user.setEmail(email);
        user.setTelephone(telephone);
        user.setPassword(password);
    }

    // ========== Getters & Setters ==========

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
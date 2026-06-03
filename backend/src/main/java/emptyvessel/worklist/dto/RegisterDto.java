package emptyvessel.worklist.dto;

import org.hibernate.validator.constraints.Length;

import emptyvessel.worklist.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterDto(
        @NotBlank @Length(min = 3, max = 30) @Pattern(regexp = "\\w+") String username,
        @NotBlank @Email String email,
        @Pattern(regexp = "^[\\d\\-+]{0,20}$") String phone,
        @jakarta.validation.constraints.NotNull User.Role role,
        @NotBlank @Length(min = 8) String password) {

    public void apply(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Target user cannot be null");
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setPasswordHash(password);
    }
}

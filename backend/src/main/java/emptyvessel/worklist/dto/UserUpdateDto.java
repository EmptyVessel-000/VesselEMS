package emptyvessel.worklist.dto;

import org.hibernate.validator.constraints.Length;

import emptyvessel.worklist.model.User;
import jakarta.validation.constraints.Email;

public record UserUpdateDto(
        @Length(min = 3, max = 30) String username,
        @Email String email,
        @Length(max = 20) String phone,
        String role,
        String password) {

    public void apply(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Target user cannot be null");
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
    }

    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }
}

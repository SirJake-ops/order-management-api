package AuctionUser.domain.serializers;

import AuctionUser.domain.exceptions.PropertyInvalidException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Embeddable
public class Email implements Serializable {
    @Column(name = "email", nullable = false)
    private String email;

    public Email(){}
    public Email(String email) throws PropertyInvalidException {
        if (isValidEmail(email)) {
            this.email = email;
        } else {
            throw new PropertyInvalidException("Invalid email");
        }
    }

    private boolean isValidEmail(String email){
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

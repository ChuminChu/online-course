package onlinecourse.login;

import onlinecourse.LoginUtils.AccessToken;
import onlinecourse.LoginUtils.LoginMember;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/admins/login")
    public AccessToken adminLogin(@RequestBody LoginRequest loginRequest){
        return loginService.adminLogin(loginRequest);
    }

    @PostMapping("/students/login")
    public AccessToken studentLogin(@RequestBody LoginRequest loginRequest){
        return loginService.studentLogin(loginRequest);
    }
}

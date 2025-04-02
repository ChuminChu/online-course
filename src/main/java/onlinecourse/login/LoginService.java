package onlinecourse.login;

import onlinecourse.LoginUtils.AccessToken;
import onlinecourse.LoginUtils.JwtProvider;
import onlinecourse.student.Student;
import onlinecourse.student.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class LoginService {

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final JwtProvider jwtProvider;

    public LoginService(StudentRepository studentRepository, AdminRepository adminRepository, JwtProvider jwtProvider) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.jwtProvider = jwtProvider;
    }

    public AccessToken adminLogin(LoginRequest loginRequest) {
        Admin admin = adminRepository.findByLoginId(loginRequest.loginId())
                .orElseThrow(()->new NoSuchElementException("아이디가 없습니다."));

        return new AccessToken(jwtProvider.createToken(admin.getLoginId()));
    }

    public AccessToken studentLogin(LoginRequest loginRequest) {
        Student student = studentRepository.findByEmail(loginRequest.loginId())
                .orElseThrow(() -> new NoSuchElementException("아이디가 없습니다."));

        return new AccessToken(jwtProvider.createToken(student.getemail()));
    }
}

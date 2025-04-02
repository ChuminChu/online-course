package onlinecourse.student;

import onlinecourse.student.dto.SignUpRequest;
import onlinecourse.student.dto.SignUpResponse;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public SignUpResponse save(SignUpRequest sign) {
        Student student = studentRepository.save(new Student(
                sign.nickName(),
                sign.email()
        ));
        return new SignUpResponse(
                student.getId(),
                student.getNickName(),
                student.getemail());
    }

    public void delete(String email, Long memberId) {
        studentRepository.findByEmail(email)
                .orElseThrow(()-> new NoSuchElementException("로그인 후 이용 가능한 서비스입니다."));

        Student student = studentRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("등록된 Id가 없습니다."));

        student.deleted();

        studentRepository.save(student);
    }
}

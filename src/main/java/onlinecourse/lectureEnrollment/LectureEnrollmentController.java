package onlinecourse.lectureEnrollment;

import onlinecourse.LoginUtils.LoginMember;
import onlinecourse.lectureEnrollment.dto.LectureEnrollmentRequest;
import onlinecourse.lectureEnrollment.dto.LectureEnrollmentResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LectureEnrollmentController {

    private final LectureEnrollmentService lectureEnrollmentService;

    public LectureEnrollmentController(LectureEnrollmentService lectureEnrollmentService) {
        this.lectureEnrollmentService = lectureEnrollmentService;
    }

    //수강신청
    @PostMapping("/lectureEnrollments")
    public LectureEnrollmentResponse select(@LoginMember String email, @RequestBody LectureEnrollmentRequest request){
        return lectureEnrollmentService.select(email, request);
    }
}

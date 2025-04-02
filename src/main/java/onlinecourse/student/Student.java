package onlinecourse.student;

import jakarta.persistence.*;
import onlinecourse.lectureEnrollment.LectureEnrollment;

import java.util.List;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String email;

    private boolean deleted = false;


    public Student() {
    }

    public Student(String nickName, String email) {
        this.nickName = nickName;
        this.email = email;

    }

    public Long getId() {
        return id;
    }

    public String getNickName() {
        return nickName;
    }


    public String getemail() {
        return email;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void deleted(){
        this.deleted = true;
    }
}

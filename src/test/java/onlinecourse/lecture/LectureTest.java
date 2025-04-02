package onlinecourse.lecture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import onlinecourse.Category;
import onlinecourse.DatabaseCleanup;
import onlinecourse.LoginUtils.AccessToken;
import onlinecourse.LoginUtils.JwtProvider;
import onlinecourse.lecture.dto.*;
import onlinecourse.lectureEnrollment.dto.LectureEnrollmentRequest;
import onlinecourse.lectureEnrollment.dto.LectureEnrollmentResponse;
import onlinecourse.login.Admin;
import onlinecourse.login.AdminRepository;
import onlinecourse.login.LoginRequest;
import onlinecourse.student.dto.SignUpRequest;
import onlinecourse.student.dto.SignUpResponse;
import onlinecourse.teacher.Teacher;
import onlinecourse.teacher.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LectureTest {

    @LocalServerPort
    int port;

    @Autowired
    DatabaseCleanup databaseCleanup;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    JwtProvider jwtProvider;

    Teacher teacher;
    Admin admin;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleanup.execute();
        //save
        teacher = new Teacher("추");
        teacherRepository.save(teacher);
        admin = new Admin("admin1234", "abcDEF123!");
        adminRepository.save(admin);

    }

    //강의 만들기
    private LectureResponse createLecture(String title, String introduce, int price, Category category, Long teacherId, AccessToken accessToken) {
        return RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.token())
                .body(new LectureCreateRequest(title,introduce,price,category,teacherId,LocalDateTime.now()))
                .when()
                .post("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LectureResponse.class);
    }

    //회원가입
    private SignUpResponse signUpStudent(String email, String password) {
        return RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest(email, password))
                .when()
                .post("/members/signup")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(SignUpResponse.class);
    }

    //수강신청
    private LectureEnrollmentResponse enrollStudentInLecture(Long lectureId, Long studentId, AccessToken accessToken) {
        return RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.token())
                .body(new LectureEnrollmentRequest(lectureId, studentId))
                .when()
                .post("/lectureEnrollments")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LectureEnrollmentResponse.class);
    }

    //학생 탈퇴
    private void deleteStudent(Long studentId, AccessToken accessToken) {
        RestAssured
                .given().log().all()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.token())
                .pathParam("memberId", studentId)
                .when()
                .delete("/members/{memberId}")
                .then().log().all()
                .statusCode(200);
    }

    //강의 상세 조회
    private LectureDetailResponse getLectureDetails(Long lectureId) {
        return RestAssured
                .given().log().all()
                .pathParam("lectureId", lectureId)
                .when()
                .get("/lectures/{lectureId}")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LectureDetailResponse.class);
    }

    //공개로 바꾸기
    private void updateLectureToPublic(Long lectureId, AccessToken accessToken) {
        RestAssured
                .given().log().all()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.token())
                .pathParam("lectureId", lectureId)
                .when()
                .patch("/lectures/{lectureId}")
                .then().log().all()
                .statusCode(200);
    }

    //강의 수정
    private LectureResponse updateLecture(Long lectureId, String title, String introduce, int price, AccessToken accessToken) {
        return RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.token())
                .pathParam("lectureId", lectureId)
                .body(new LectureUpdateRequest(
                        title,
                        introduce,
                        price,
                        LocalDateTime.now()
                ))
                .when()
                .put("/lectures/{lectureId}")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LectureResponse.class);
    }


    //강의 삭제
    private void deleteLecture(Long lectureId, AccessToken accessToken) {
        RestAssured
                .given().log().all()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.token())
                .when()
                .pathParam("lectureId", lectureId)
                .delete("/lectures/{lectureId}")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 강의등록() {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture = createLecture(
                "자바 배우기",
                "자바, Spring을 통한 웹 개발 강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );

        assertThat(lecture.id()).isEqualTo(1);
        assertThat(lecture.category()).isEqualTo(Category.Math);
        assertThat(lecture.isPrivate()).isTrue();
    }

    @Test
    void 비공개_강의목록조회() throws InterruptedException {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        createLecture(
                "자바 배우기",
                "자바, Spring을 통한 웹 개발 강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );
        Thread.sleep(1000);

        createLecture(
                "자바 응용하기",
                "자바, Spring을 통한 웹 개발 실습강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );
        Thread.sleep(1000);

        createLecture(
                "과학 배우기",
                "과학 강의입니다.",
                50000,
                Category.Science,
                teacher.getId(),
                accessToken
        );

        List<LectureListResponse> list = RestAssured
                .given().log().all()
                .when()
                .get("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", LectureListResponse.class);

        assertThat(list.size()).isEqualTo(0);
        assertThat(list.isEmpty()).isTrue();
    }

    @Test
    void 공개_강의목록조회() throws InterruptedException {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture1 = createLecture(
                "자바 배우기",
                "자바, Spring을 통한 웹 개발 강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );
        Thread.sleep(1000);

        LectureResponse lecture2 = createLecture(
                "자바 응용하기",
                "자바, Spring을 통한 웹 개발 실습강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );
        Thread.sleep(1000);

        LectureResponse lecture3 = createLecture(
                "과학 배우기",
                "과학 강의입니다.",
                50000,
                Category.Science,
                teacher.getId(),
                accessToken
        );

        updateLectureToPublic(lecture1.id(),accessToken);
        updateLectureToPublic(lecture2.id(),accessToken);

        List<LectureListResponse> list = RestAssured
                .given().log().all()
                .when()
                .get("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", LectureListResponse.class);

        assertThat(list.size()).isEqualTo(2);
    }


    @Test
    void 비공개_강의상세조회() {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture1 = createLecture(
                "자바 배우기",
                "자바, Spring을 통한 웹 개발 강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );

        SignUpResponse student = signUpStudent("chu@gmail.com", "chuchu");

        AccessToken token = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("chu@gmail.com", "chuchu"))
                .when()
                .post("/students/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);


        LectureEnrollmentResponse 수강신청 = enrollStudentInLecture(lecture1.id(), student.id(), token);

        LectureDetailResponse lectureDetails = getLectureDetails(lecture1.id());

        assertThat(lectureDetails.title()).isEqualTo("자바 배우기");
        assertThat(lectureDetails.introduce()).isEqualTo("자바, Spring을 통한 웹 개발 강의입니다.");
        assertThat(lectureDetails.price()).isEqualTo(50000);
        assertThat(lectureDetails.studentCount()).isEqualTo(1);
        assertThat(lectureDetails.students().get(0).nickName()).isEqualTo("chuchu");
    }


    @Test
    void 강의수정() {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture1 = createLecture(
                "자바 배우기",
                "자바, Spring을 통한 웹 개발 강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );

        LectureResponse 수정된강의 = updateLecture(
                lecture1.id(),
                "수정된 이름",
                "수정된 소개",
                1000,
                accessToken
        );

        LectureDetailResponse lectureId = getLectureDetails(수정된강의.id());

        assertThat(수정된강의.introduce()).isEqualTo("수정된 소개");
        assertThat(lectureId.updateTime()).isEqualTo(수정된강의.createTime());
    }


    @Test
    void 강의삭제() {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture1 = createLecture(
                "자바 배우기",
                "자바, Spring을 통한 웹 개발 강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );
        deleteLecture(lecture1.id(),accessToken);
    }


    @Test
    void 공개된_삭제된_강의목록_빼고_조회() {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture1 = createLecture(
                "자바 배우기",
                "자바, Spring을 통한 웹 개발 강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );

        updateLectureToPublic(lecture1.id(),accessToken);

        LectureResponse lecture2 = createLecture(
                "자바 응용하기",
                "자바, Spring을 통한 웹 개발 실습강의입니다.",
                50000,
                Category.Math,
                teacher.getId(),
                accessToken
        );

        updateLectureToPublic(lecture2.id(),accessToken);

        LectureResponse lecture3 = createLecture(
                "과학 배우기",
                "과학 강의입니다.",
                50000,
                Category.Science,
                teacher.getId(),
                accessToken
        );

        updateLectureToPublic(lecture3.id(),accessToken);

        deleteLecture(lecture1.id(),accessToken);

        List<LectureListResponse> list = RestAssured
                .given().log().all()
                .when()
                .get("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", LectureListResponse.class);

        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    void 삭제된_강의상세조회x() {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture = createLecture(
                "과학 배우기",
                "과학 강의입니다.",
                50000,
                Category.Science,
                teacher.getId(),
                accessToken
        );

        deleteLecture(lecture.id(),accessToken);

        RestAssured
                .given().log().all()
                .pathParam("lectureId", lecture.id())
                .when()
                .get("/lectures/{lectureId}")
                .then().log().all()
                .statusCode(500);
    }

    @Test
    void 삭제된_강의목록_수정x() {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture = createLecture(
                "과학 배우기",
                "과학 강의입니다.",
                50000,
                Category.Science,
                teacher.getId(),
                accessToken
        );

        deleteLecture(lecture.id(),accessToken);

        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("lectureId", lecture.id())
                .body(new LectureUpdateRequest(
                        "수정된 이름",
                        "수정된 소개",
                        1000,
                        LocalDateTime.now()
                ))
                .when()
                .put("/lectures/{lectureId}")
                .then().log().all()
                .statusCode(500);
    }

    @Test
    void 공개된_강의_제목_검색() {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture1 = createLecture(
                "자바 배우기", "자바, Spring을 통한 웹 개발 강의입니다.",
                50000, Category.Math, teacher.getId(),accessToken
        );

        LectureResponse lecture2 = createLecture(
                "자바 응용하기", "자바, Spring을 통한 웹 개발 실습강의입니다.",
                50000, Category.Math, teacher.getId(),accessToken
        );

        LectureResponse lecture3 = createLecture(
                "과학 배우기", "과학 강의입니다.",
                50000, Category.Science, teacher.getId(),accessToken
        );

        updateLectureToPublic(lecture1.id(),accessToken);
        updateLectureToPublic(lecture2.id(),accessToken);
        updateLectureToPublic(lecture3.id(),accessToken);

        List<LectureListResponse> list = RestAssured
                .given().log().all()
                .param("title", "자바")
                .param("teacherName", teacher.getName())
                .when()
                .get("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", LectureListResponse.class);

        List<LectureListResponse> list1 = RestAssured
                .given().log().all()
                .when()
                .get("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", LectureListResponse.class);


        assertThat(list.size()).isEqualTo(2);
        assertThat(list1.size()).isEqualTo(3);
        assertThat(list.stream().allMatch(l -> l.title().contains("자바"))).isTrue();
        assertThat(list.stream().allMatch(l -> l.teacherName().contains(teacher.getName()))).isTrue();
    }

    @Test
    void 검색_추가로_카테고리_필터가능() {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);

        LectureResponse lecture1 = createLecture(
                "자바 배우기", "자바, Spring을 통한 웹 개발 강의입니다.",
                50000, Category.Math, teacher.getId(),accessToken
        );

        LectureResponse lecture2 = createLecture(
                "자바 응용하기", "자바, Spring을 통한 웹 개발 실습강의입니다.",
                50000, Category.Math, teacher.getId(),accessToken
        );

        LectureResponse lecture3 = createLecture(
                "과학 배우기", "과학 강의입니다.",
                50000, Category.Science, teacher.getId(),accessToken
        );

        updateLectureToPublic(lecture1.id(),accessToken);
        updateLectureToPublic(lecture2.id(),accessToken);
        updateLectureToPublic(lecture3.id(),accessToken);

        List<LectureListResponse> list = RestAssured
                .given().log().all()
                .param("title", "자바")
                .param("category", Category.Math)
                .when()
                .get("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", LectureListResponse.class);

        assertThat(list.size()).isEqualTo(2);
        assertThat(list.stream().allMatch(l->l.category().equals(Category.Math))).isTrue();
        assertThat(list.stream().allMatch(l->l.title().contains("자바"))).isTrue();
    }

    @Test
    void 강의페이지() throws InterruptedException {
        AccessToken accessToken = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(
                        "admin1234",
                        "abcDEF123!"))
                .when()
                .post("/admins/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AccessToken.class);
        LectureResponse lecture1 = createLecture("자바 배우기", "자바, Spring을 통한 웹 개발 강의입니다.", 50000, Category.Math, teacher.getId(),accessToken);
        Thread.sleep(1);
        LectureResponse lecture2 = createLecture("자바 응용하기", "자바, Spring을 통한 웹 개발 실습강의입니다.", 50000, Category.Math, teacher.getId(),accessToken);
        Thread.sleep(1);
        LectureResponse lecture3 = createLecture("과학 배우기", "과학 강의입니다.", 50000, Category.Science, teacher.getId(),accessToken);
        Thread.sleep(1);
        LectureResponse lecture4 = createLecture("파이썬 기초", "파이썬을 통한 프로그래밍 기초 강의입니다.", 40000, Category.English,teacher.getId(),accessToken);
        Thread.sleep(1);
        LectureResponse lecture5 = createLecture("AI 응용", "AI 및 머신러닝 강의입니다.", 60000, Category.Math, teacher.getId(),accessToken);

        updateLectureToPublic(lecture1.id(),accessToken);
        updateLectureToPublic(lecture2.id(),accessToken);
        updateLectureToPublic(lecture3.id(),accessToken);
        updateLectureToPublic(lecture4.id(),accessToken);
        updateLectureToPublic(lecture5.id(),accessToken);
        int page = 1;
        int size = 2;

        List<LectureListResponse> list = RestAssured
                .given().log().all()
                .param("page", page)
                .param("size", size)
                .when()
                .get("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", LectureListResponse.class);

        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).title()).isEqualTo("AI 응용");
        assertThat(list.get(1).title().equals("과학 배우기")).isFalse();

        page=2;
        List<LectureListResponse> list2 = RestAssured
                .given().log().all()
                .param("page", page)
                .param("size", size)
                .when()
                .get("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", LectureListResponse.class);

        assertThat(list2.size()).isEqualTo(2);
        assertThat(list2.get(0).title()).isEqualTo("과학 배우기");

        page=3;
        List<LectureListResponse> list3 = RestAssured
                .given().log().all()
                .param("page", page)
                .param("size", size)
                .when()
                .get("/lectures")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", LectureListResponse.class);

        assertThat(list3.size()).isEqualTo(1);
        assertThat(list3.get(0).title()).isEqualTo("자바 배우기");
    }
}

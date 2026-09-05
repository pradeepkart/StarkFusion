package com.skillgap.analyzer;

import com.skillgap.analyzer.entity.*;
import com.skillgap.analyzer.repository.*;
import com.skillgap.analyzer.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired UserRepository users;
    @Autowired StudentRepository students;
    @Autowired SkillRepository skills;
    @Autowired JobRepository jobs;
    @Autowired StudentSkillRepository studentSkills;
    @Autowired JobSkillRepository jobSkills;
    @Autowired ApplicationRepository applications;
    @Autowired RecommendationRepository recommendations;
    @Autowired BCryptPasswordEncoder encoder;
    @Autowired JwtService jwt;
    @Value("${jwt.secret}") String signingSecret;

    @BeforeEach
    void resetApplicationData() {
        recommendations.deleteAll(); applications.deleteAll(); studentSkills.deleteAll(); jobSkills.deleteAll();
        students.deleteAll(); skills.deleteAll(); jobs.deleteAll();
        users.deleteAll(users.findAll().stream().filter(u -> u.getRole() == Role.ROLE_USER).toList());
    }

    private JsonNode send(MockHttpServletRequestBuilder request, String token, Object body, int expected) throws Exception {
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var result = mvc.perform(request).andExpect(status().is(expected)).andReturn();
        assertThat(result.getResponse().getHeader("Set-Cookie")).isNull();
        assertThat(result.getRequest().getSession(false)).isNull();
        String content = result.getResponse().getContentAsString();
        assertThat(content).doesNotContain("$2a$", "$2b$");
        JsonNode parsed = content.isBlank() ? null : json.readTree(content);
        if (parsed != null) assertThat(parsed.has("password")).isFalse();
        return parsed;
    }
    private void register(String email) throws Exception {
        var result = send(post("/api/auth/register"), null, Map.of("name", "Student", "email", email, "password", "123456"), 201);
        assertThat(result.get("message").asText()).isEqualTo("User registered successfully");
    }
    private String login(String email, String password) throws Exception {
        return send(post("/api/auth/login"), null, Map.of("email", email, "password", password), 200).get("token").asText();
    }
    private String user(String email) throws Exception { register(email); return login(email, "123456"); }
    private String admin() throws Exception { return login("admin@skillgap.com", "admin123"); }
    private long skill(String admin, String name) throws Exception {
        return send(post("/api/admin/skills"), admin, Map.of("name", name, "category", "Technical"), 201).get("skillId").asLong();
    }
    private long job(String admin) throws Exception {
        return send(post("/api/admin/jobs"), admin, Map.of("company", "ABC", "title", "Java Developer", "location", "Chennai"), 201).get("jobId").asLong();
    }
    private void requirement(String admin, long job, long skill, int level, boolean mandatory) throws Exception {
        send(post("/api/admin/jobs/" + job + "/skills"), admin, Map.of("skillId", skill, "requiredLevel", level, "mandatory", mandatory), 200);
    }
    private void proficiency(String token, long skill, int level) throws Exception {
        send(post("/api/user/skills"), token, Map.of("skillId", skill, "proficiency", level), 200);
    }

    @Test void registrationLinksProfileHashesPasswordsAndNeverAllowsAdminRegistration() throws Exception {
        register("Student@example.com");
        var user = users.findByEmail("student@example.com").orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(encoder.matches("123456", user.getPassword())).isTrue();
        assertThat(students.findByUserEmail(user.getEmail())).isPresent();
        send(post("/api/auth/register"), null, Map.of("name", "Other", "email", "STUDENT@example.com", "password", "123456"), 409);
        send(post("/api/auth/register"), null, Map.of("name", "Evil", "email", "evil@example.com", "password", "123456", "role", "ROLE_ADMIN"), 400);
        assertThat(users.findByEmail("evil@example.com")).isEmpty();
        var admin = users.findByEmail("admin@skillgap.com").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(encoder.matches("admin123", admin.getPassword())).isTrue();
    }

    @Test void loginReturnsExpectedClaimsAndTwentyFourHourExpiry() throws Exception {
        register("student@example.com");
        var response = send(post("/api/auth/login"), null, Map.of("email", "STUDENT@example.com", "password", "123456"), 200);
        assertThat(response.get("type").asText()).isEqualTo("Bearer");
        assertThat(response.get("role").asText()).isEqualTo("ROLE_USER");
        assertThat(response.get("name").asText()).isEqualTo("Student");
        String token = response.get("token").asText();
        assertThat(jwt.extractUsername(token)).isEqualTo("student@example.com");
        assertThat(jwt.extractRole(token)).isEqualTo("ROLE_USER");
        assertThat(jwt.extractExpiration(token).getTime() - System.currentTimeMillis()).isBetween(86_390_000L, 86_400_000L);
        send(post("/api/auth/login"), null, Map.of("email", "student@example.com", "password", "wrong-password"), 401);
        send(post("/api/auth/login"), null, Map.of("email", "missing@example.com", "password", "123456"), 401);
    }

    @Test void requiresAuthenticationAndEnforcesBothRolesAndLegacyRouteLockdown() throws Exception {
        String user = user("student@example.com"), admin = admin();
        for (String path : new String[]{"/api/user/profile", "/api/admin/dashboard", "/api/jobs", "/api/skills"}) {
            send(get(path), null, null, 401);
        }
        send(get("/api/admin/dashboard"), user, null, 403);
        send(post("/api/admin/jobs"), user, Map.of("company", "X", "title", "Y", "location", "Z"), 403);
        send(get("/api/user/profile"), admin, null, 403);
        send(get("/api/admin/dashboard"), admin, null, 200);
        send(get("/api/jobs"), user, null, 200);
        send(get("/api/skills"), admin, null, 200);
        send(post("/api/students"), user, Map.of("name", "Other", "email", "other@example.com"), 403);
        send(post("/api/applications"), user, Map.of("studentId", 1, "jobId", 1), 403);
        send(post("/api/skills"), user, Map.of("name", "X", "category", "Y"), 403);
    }

    @Test void rejectsMalformedExpiredWrongSignatureMissingExpiryAndRoleMismatchTokens() throws Exception {
        user("student@example.com");
        var key = Keys.hmacShaKeyFor(signingSecret.getBytes(StandardCharsets.UTF_8));
        String expired = Jwts.builder().subject("student@example.com").claim("role", "ROLE_USER")
                .expiration(new Date(System.currentTimeMillis() - 5000)).signWith(key).compact();
        String missingExpiry = Jwts.builder().subject("student@example.com").claim("role", "ROLE_USER").signWith(key).compact();
        String wrongRole = Jwts.builder().subject("student@example.com").claim("role", "ROLE_ADMIN")
                .expiration(new Date(System.currentTimeMillis() + 60000)).signWith(key).compact();
        String wrongKey = Jwts.builder().subject("student@example.com").claim("role", "ROLE_USER")
                .expiration(new Date(System.currentTimeMillis() + 60000)).signWith(Jwts.SIG.HS256.key().build()).compact();
        for (String token : new String[]{"not-a-jwt", expired, missingExpiry, wrongRole, wrongKey}) {
            var error = send(get("/api/user/profile"), token, null, 401);
            assertThat(error.get("message").asText()).isEqualTo("Invalid or expired token");
        }
    }

    @Test void fullWorkflowPersistsSnapshotsAndAdminManagesStatus() throws Exception {
        String admin = admin(), user = user("student@example.com");
        long java = skill(admin, "Java"), sql = skill(admin, "MySQL"), job = job(admin);
        requirement(admin, job, java, 4, true); requirement(admin, job, sql, 3, false);
        proficiency(user, java, 2); proficiency(user, sql, 3);
        var result = send(get("/api/user/jobs/" + job + "/skill-gap"), user, null, 200);
        assertThat(result.get("overallMatchPercent").asDouble()).isEqualTo(66.67);
        assertThat(result.get("jobTitle").asText()).isEqualTo("Java Developer");
        var advice = send(get("/api/user/jobs/" + job + "/recommendations"), user, null, 200);
        assertThat(advice.size()).isEqualTo(1);
        assertThat(advice.get(0).get("priority").asInt()).isEqualTo(1);
        assertThat(recommendations.count()).isEqualTo(1);
        var application = send(post("/api/user/applications"), user, Map.of("jobId", job), 201);
        assertThat(application.get("matchPercent").asDouble()).isEqualTo(66.67);
        assertThat(application.get("status").asText()).isEqualTo("APPLIED");
        send(post("/api/user/applications"), user, Map.of("jobId", job), 409);
        proficiency(user, java, 5);
        assertThat(studentSkills.count()).isEqualTo(2);
        assertThat(recommendations.count()).isZero();
        var current = send(get("/api/user/jobs/" + job + "/skill-gap"), user, null, 200);
        assertThat(current.get("overallMatchPercent").asDouble()).isEqualTo(100);
        long id = application.get("id").asLong();
        send(put("/api/admin/applications/" + id + "/status"), admin, Map.of("status", "SELECTED"), 200);
        var myApplications = send(get("/api/user/applications"), user, null, 200);
        assertThat(myApplications.get(0).get("matchPercent").asDouble()).isEqualTo(66.67);
        assertThat(myApplications.get(0).get("status").asText()).isEqualTo("SELECTED");
        var dashboard = send(get("/api/admin/dashboard"), admin, null, 200);
        assertThat(dashboard.get("totalStudents").asInt()).isEqualTo(1);
        assertThat(dashboard.get("totalJobs").asInt()).isEqualTo(1);
        assertThat(dashboard.get("totalApplications").asInt()).isEqualTo(1);
        assertThat(dashboard.get("averageSkillMatch").asDouble()).isEqualTo(66.67);
        send(put("/api/admin/applications/" + id + "/status"), user, Map.of("status", "SELECTED"), 403);
        send(put("/api/admin/applications/" + id + "/status"), admin, Map.of("status", "HIRED"), 400);
    }

    @Test void twoUsersCannotReadOrMutateEachOthersSkillsOrApplications() throws Exception {
        String admin = admin(), alice = user("alice@example.com"), bob = user("bob@example.com");
        long skill = skill(admin, "Java"), job = job(admin);
        requirement(admin, job, skill, 4, true);
        proficiency(alice, skill, 3);
        var aliceProfile = send(get("/api/user/profile"), alice, null, 200);
        var bobProfile = send(get("/api/user/profile"), bob, null, 200);
        assertThat(aliceProfile.get("studentId")).isNotEqualTo(bobProfile.get("studentId"));
        assertThat(send(get("/api/user/skills"), bob, null, 200).size()).isZero();
        send(delete("/api/user/skills/" + skill), bob, null, 404);
        send(post("/api/user/applications"), alice, Map.of("jobId", job), 201);
        assertThat(send(get("/api/user/applications"), bob, null, 200).size()).isZero();
        send(post("/api/user/applications"), bob, Map.of("jobId", job, "studentId", aliceProfile.get("studentId").asLong()), 400);
        send(post("/api/user/skills"), bob, Map.of("skillId", skill, "proficiency", 5, "studentId", aliceProfile.get("studentId").asLong()), 400);
        var bobApplication = send(post("/api/user/applications"), bob, Map.of("jobId", job), 201);
        assertThat(bobApplication.get("studentId")).isEqualTo(bobProfile.get("studentId"));
        assertThat(send(get("/api/admin/applications"), admin, null, 200).size()).isEqualTo(2);
    }

    @Test void validatesInputsAndGivesConsistentErrors() throws Exception {
        send(post("/api/auth/register"), null, Map.of("name", "", "email", "bad", "password", "short"), 400);
        send(post("/api/auth/register"), null, Map.of("name", "X", "email", "x@example.com", "password", "界".repeat(30)), 400);
        String admin = admin(), user = user("student@example.com");
        long skill = skill(admin, "Java"), job = job(admin);
        for (Object level : new Object[]{0, -1, 6, 2.5}) {
            send(post("/api/user/skills"), user, Map.of("skillId", skill, "proficiency", level), 400);
            send(post("/api/admin/jobs/" + job + "/skills"), admin, Map.of("skillId", skill, "requiredLevel", level, "mandatory", true), 400);
        }
        send(post("/api/user/skills"), user, Map.of("skillId", skill), 400);
        send(post("/api/admin/jobs/" + job + "/skills"), admin, Map.of("skillId", skill, "requiredLevel", 3), 400);
        send(post("/api/user/skills"), user, Map.of("skillId", 999999, "proficiency", 3), 404);
        send(get("/api/user/jobs/-1"), user, null, 400);
        var missing = send(get("/api/user/jobs/999999"), user, null, 404);
        assertThat(missing.get("status").asInt()).isEqualTo(404);
        assertThat(missing.get("message").asText()).isEqualTo("Job not found with id 999999");
        send(post("/api/user/applications"), user, Map.of("jobId", job, "matchPercent", 100), 400);
    }

    @Test void adminCrudAndInputChangesMaintainRecommendationsAndForeignKeys() throws Exception {
        String admin = admin(), user = user("student@example.com");
        long skill = skill(admin, "Java"), job = job(admin);
        requirement(admin, job, skill, 4, true);
        proficiency(user, skill, 2);
        requirement(admin, job, skill, 3, true);
        assertThat(jobSkills.count()).isEqualTo(1);
        assertThat(send(get("/api/user/jobs/" + job + "/recommendations"), user, null, 200).get(0).get("priority").asInt()).isEqualTo(2);
        send(put("/api/admin/skills/" + skill), admin, Map.of("name", "Java SE", "category", "Programming"), 200);
        assertThat(recommendations.findAll().getFirst().getReason()).contains("Java SE");
        send(put("/api/admin/jobs/" + job), admin, Map.of("title", "Backend Developer", "company", "ABC", "location", "Remote"), 200);
        send(delete("/api/admin/skills/" + skill), admin, null, 409);
        long studentId = send(get("/api/user/profile"), user, null, 200).get("studentId").asLong();
        assertThat(send(get("/api/admin/students"), admin, null, 200).size()).isEqualTo(1);
        send(get("/api/admin/students/" + studentId), admin, null, 200);
        assertThat(send(get("/api/admin/students/" + studentId + "/skills"), admin, null, 200).size()).isEqualTo(1);
        send(delete("/api/user/skills/" + skill), user, null, 204);
        assertThat(recommendations.findAll().getFirst().getReason()).contains("Current level is 0");
        send(delete("/api/admin/jobs/" + job + "/skills/" + skill), admin, null, 204);
        assertThat(recommendations.count()).isZero();
        send(delete("/api/admin/skills/" + skill), admin, null, 204);
        send(delete("/api/admin/jobs/" + job), admin, null, 204);
        assertThat(jobs.count()).isZero();
    }

    @Test void emptyJobsHaveZeroScoreAndApplicationsBlockJobDeletion() throws Exception {
        String admin = admin(), user = user("student@example.com");
        long job = job(admin);
        var result = send(get("/api/user/jobs/" + job + "/skill-gap"), user, null, 200);
        assertThat(result.get("evaluable").asBoolean()).isFalse();
        assertThat(result.get("overallMatchPercent").asDouble()).isZero();
        send(post("/api/user/applications"), user, Map.of("jobId", job), 201);
        send(delete("/api/admin/jobs/" + job), admin, null, 409);
    }

    @Test void corsAllowsAuthorizationHeaderForConfiguredFrontend() throws Exception {
        mvc.perform(options("/api/user/skills").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/user/skills").header("Origin", "https://unconfigured.example")
                .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
    }
}

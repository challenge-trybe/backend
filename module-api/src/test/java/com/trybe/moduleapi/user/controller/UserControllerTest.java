package com.trybe.moduleapi.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.auth.CustomUserDetailsService;
import com.trybe.moduleapi.auth.jwt.JwtUtils;
import com.trybe.moduleapi.auth.jwt.exception.CustomAccessDeniedHandler;
import com.trybe.moduleapi.auth.jwt.exception.CustomAuthenticationEntryPoint;
import com.trybe.moduleapi.config.SecurityConfig;
import com.trybe.moduleapi.user.dto.request.UserRequest;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.moduleapi.user.exception.DuplicatedUserException;
import com.trybe.moduleapi.user.exception.NotFoundUserException;
import com.trybe.moduleapi.user.exception.UpdatePasswordFailException;
import com.trybe.moduleapi.user.fixtures.AuthenticationFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.moduleapi.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {
    private String docsPath = "user-controller-test/";
    private final String invalidBadRequestPath = "invalid/bad-request/";
    private final String invalidNotFoundPath = "invalid/not-found/";
    private final String invalidDuplicatedUserIdPath = "invalid/duplicated-userId/";
    private final String invalidDuplicatedEmailPath = "invalid/duplicated-email/";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("정상적인 회원가입 요청 시 200을 반환한다.")
    void 정상적인_회원가입_요청_시_200을_반환한다() throws Exception {
        UserResponse 회원_응답 = UserFixtures.회원_응답;

        when(userService.save(any(UserRequest.Create.class))).thenReturn(회원_응답);

        mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .content(objectMapper.writeValueAsString(UserFixtures.회원가입_요청)))
               .andExpect(status().isOk())
               .andExpectAll(
                       jsonPath("$.id").value(회원_응답.id()),
                       jsonPath("$.nickname").value(회원_응답.nickname()),
                       jsonPath("$.email").value(회원_응답.email()),
                       jsonPath("$.userId").value(회원_응답.userId()),
                       jsonPath("$.gender").value(회원_응답.gender().toString()),
                       jsonPath("$.birth").value(회원_응답.birth().toString())
               )
               .andDo(document(docsPath + "save",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("userId").type(JsonFieldType.STRING).description("아이디"),
                                       fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                       fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                       fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                       fieldWithPath("gender").type(JsonFieldType.STRING).description("성별"),
                                       fieldWithPath("birth").type(JsonFieldType.STRING).description("생년월일 (형식: YYYY-MM-DD)")
                               ),
                               responseFields(
                                       fieldWithPath("id").type(JsonFieldType.NUMBER).description("PK"),
                                       fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                       fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                       fieldWithPath("userId").type(JsonFieldType.STRING).description("아이디"),
                                       fieldWithPath("gender").type(JsonFieldType.STRING).description("성별"),
                                       fieldWithPath("birth").type(JsonFieldType.STRING).description("생년월일 (형식: YYYY-MM-DD)")
                               )
               ));
    }

    @Test
    @DisplayName("유효성 검증에 실패하는 비정상적인 회원가입 요청 시 400을 반환한다.")
    void 유효성_검증에_실패하는_비정상적인_회원가입_요청_시_400을_반환한다() throws Exception {
        UserRequest.Create 잘못된_회원가입_요청 = UserFixtures.잘못된_회원가입_요청;
        mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(잘못된_회원가입_요청)))
               .andExpect(status().isBadRequest())
               .andExpectAll(
                       jsonPath("$.status").value(400),
                       jsonPath("$.message").doesNotExist(),
                       jsonPath("$.data.password").exists(),
                       jsonPath("$.data.nickname").exists(),
                       jsonPath("$.data.userId").exists(),
                       jsonPath("$.data.birth").exists(),
                       jsonPath("$.data.email").exists()
               )
               .andDo(document(docsPath + "save/" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("userId").description("아이디"),
                                       fieldWithPath("email").description("이메일"),
                                       fieldWithPath("nickname").description("닉네임"),
                                       fieldWithPath("password").description("비밀번호"),
                                       fieldWithPath("gender").description("성별"),
                                       fieldWithPath("birth").description("생년월일")
                               ),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("메시지").optional(),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("회원가입 유효성 검증 실패 에러 메시지"),
                                       fieldWithPath("data.password").type(JsonFieldType.STRING).description("회원가입 비밀번호 검증 에러 메시지"),
                                       fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("회원가입 닉네임 검증 에러 메시지"),
                                       fieldWithPath("data.gender").type(JsonFieldType.STRING).description("회원가입 성별 검증 에러 메시지"),
                                       fieldWithPath("data.userId").type(JsonFieldType.STRING).description("회원가입 아이디 검증 에러 메시지"),
                                       fieldWithPath("data.birth").type(JsonFieldType.STRING).description("회원가입 생년월일 검증 에러 메시지"),
                                       fieldWithPath("data.email").type(JsonFieldType.STRING).description("회원가입 이메일 검증 에러 메시지")
                               )
               ));
    }

    @Test
    @DisplayName("중복 아이디로 회원가입 요청 시 409을 반환한다.")
    void 중복_아이디로_회원가입_요청_시_409을_반환한다() throws Exception {
        UserRequest.Create 중복된_아이디_회원가입_요청 = UserFixtures.회원가입_요청;
        doThrow(new DuplicatedUserException("이미 존재하는 아이디입니다.")).when(userService).save(중복된_아이디_회원가입_요청);
        mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(중복된_아이디_회원가입_요청)))
               .andExpect(status().isConflict())
               .andExpectAll(
                       jsonPath("$.status").value(409),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data.password").doesNotExist()
               )
               .andDo(document(docsPath + "save/" + invalidDuplicatedUserIdPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("userId").description("아이디"),
                                       fieldWithPath("email").description("이메일"),
                                       fieldWithPath("nickname").description("닉네임"),
                                       fieldWithPath("password").description("비밀번호"),
                                       fieldWithPath("gender").description("성별"),
                                       fieldWithPath("birth").description("생년월일")
                               ),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                               )
               ));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 요청 시 409을 반환한다.")
    void 중복_이메일로_회원가입_요청_시_409을_반환한다() throws Exception {
        UserRequest.Create 중복된_이메일_회원가입_요청 = UserFixtures.회원가입_요청;
        doThrow(new DuplicatedUserException("이미 존재하는 이메일입니다.")).when(userService).save(중복된_이메일_회원가입_요청);
        mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(중복된_이메일_회원가입_요청)))
               .andExpect(status().isConflict())
               .andExpectAll(
                       jsonPath("$.status").value(409),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data.password").doesNotExist()
               )
               .andDo(document(docsPath + "save/" + invalidDuplicatedEmailPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("userId").description("아이디"),
                                       fieldWithPath("email").description("이메일"),
                                       fieldWithPath("nickname").description("닉네임"),
                                       fieldWithPath("password").description("비밀번호"),
                                       fieldWithPath("gender").description("성별"),
                                       fieldWithPath("birth").description("생년월일")
                               ),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                               )
               ));
    }

    @Test
    @DisplayName("존재하는 회원 조회 시 200을 반환한다.")
    void 존재하는_회원_조회_시_200을_반환한다() throws Exception {
        UserResponse 회원_응답 = UserFixtures.회원_응답;
        when(userService.findById(1L)).thenReturn(회원_응답);
        CustomUserDetails principalDetails = new CustomUserDetails(UserFixtures.회원);

        mockMvc.perform(get("/api/v1/users/{id}", 1L)
                                .header("Authorization", "Bearer "+AuthenticationFixtures.accessToken)
                                .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
               .andExpect(status().isOk())
               .andExpectAll(
                       jsonPath("$.id").value(회원_응답.id()),
                       jsonPath("$.nickname").value(회원_응답.nickname()),
                       jsonPath("$.email").value(회원_응답.email()),
                       jsonPath("$.userId").value(회원_응답.userId()),
                       jsonPath("$.gender").value(회원_응답.gender().toString()),
                       jsonPath("$.birth").value(회원_응답.birth().toString())
               )
               .andDo(document(docsPath + "findById",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("id").description("조회할 회원 ID")),
                               responseFields(
                                       fieldWithPath("id").type(JsonFieldType.NUMBER).description("PK"),
                                       fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                       fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                       fieldWithPath("userId").type(JsonFieldType.STRING).description("아이디"),
                                       fieldWithPath("gender").type(JsonFieldType.STRING).description("성별"),
                                       fieldWithPath("birth").type(JsonFieldType.STRING).description("생년월일 (형식: YYYY-MM-DD)")
                               )
               ));
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 404를 반환한다.")
    void 존재하지_않는_회원_조회_시_404를_반환한다() throws Exception {
        doThrow(new NotFoundUserException()).when(userService).findById(10L);
        CustomUserDetails principalDetails = new CustomUserDetails(UserFixtures.회원);

        mockMvc.perform(get("/api/v1/users/{id}", 10L)
                                .header("Authorization", "Bearer "+AuthenticationFixtures.accessToken)
                                .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               )
               .andDo(document(docsPath + "findById/" + invalidNotFoundPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("id").description("조회할 회원 ID")),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                               )
               ));
    }

    @Test
    @DisplayName("정상적인 회원 정보 수정 시 200을 반환한다.")
    void 정상적인_회원_정보_수정_시_200을_반환한다() throws Exception {
        UserRequest.Update 회원정보_수정_요청 = UserFixtures.회원정보_수정_요청;
        UserResponse 회원_응답 = UserFixtures.수정된_회원_응답;
        CustomUserDetails principalDetails = new CustomUserDetails(UserFixtures.회원);

        when(userService.updateProfile(
                any(CustomUserDetails.class),
                any(UserRequest.Update.class))).thenReturn(UserFixtures.수정된_회원_응답);

        mockMvc.perform(put("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .content(objectMapper.writeValueAsString(회원정보_수정_요청))
                                .header("Authorization", "Bearer "+AuthenticationFixtures.accessToken)
                                .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
               .andExpect(status().isOk())
               .andExpectAll(
                       jsonPath("$.id").value(회원_응답.id()),
                       jsonPath("$.nickname").value(회원_응답.nickname()),
                       jsonPath("$.email").value(회원_응답.email()),
                       jsonPath("$.userId").value(회원_응답.userId()),
                       jsonPath("$.gender").value(회원_응답.gender().toString()),
                       jsonPath("$.birth").value(회원_응답.birth().toString())
               )
               .andDo(document(docsPath + "user-update-profile",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                       fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                       fieldWithPath("gender").type(JsonFieldType.STRING).description("성별"),
                                       fieldWithPath("birth").type(JsonFieldType.STRING).description("생년월일 (형식: YYYY-MM-DD)")
                               ),
                               responseFields(
                                       fieldWithPath("id").type(JsonFieldType.NUMBER).description("PK"),
                                       fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                       fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                       fieldWithPath("userId").type(JsonFieldType.STRING).description("아이디"),
                                       fieldWithPath("gender").type(JsonFieldType.STRING).description("성별"),
                                       fieldWithPath("birth").type(JsonFieldType.STRING).description("생년월일 (형식: YYYY-MM-DD)")
                               )
               ));
    }

    @Test
    @DisplayName("유효성 검증에 실패하는 회원정보 수정 요청 시 400을 반환한다.")
    void 유효성_검증에_실패하는_회원정보_수정_요청_시_400을_반환한다() throws Exception {
        UserRequest.Update 잘못된_회원가입_요청 = UserFixtures.잘못된_회원정보_수정_요청;
        CustomUserDetails principalDetails = new CustomUserDetails(UserFixtures.회원);

        mockMvc.perform(put("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(잘못된_회원가입_요청))
                                .header("Authorization", "Bearer "+AuthenticationFixtures.accessToken)
                                .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
               .andExpect(status().isBadRequest())
               .andExpectAll(
                       jsonPath("$.status").value(400),
                       jsonPath("$.message").doesNotExist(),
                       jsonPath("$.data.nickname").exists(),
                       jsonPath("$.data.birth").exists(),
                       jsonPath("$.data.email").exists(),
                       jsonPath("$.data.gender").exists()
               )
               .andDo(document(docsPath + "user-update-profile/" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("email").description("이메일"),
                                       fieldWithPath("nickname").description("닉네임"),
                                       fieldWithPath("gender").description("성별"),
                                       fieldWithPath("birth").description("생년월일 (형식: YYYY-MM-DD)")
                               ),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional(),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("회원정보 유효성 검증 실패 에러 메시지"),
                                       fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("회원정보 닉네임 검증 에러 메시지"),
                                       fieldWithPath("data.gender").type(JsonFieldType.STRING).description("회원정보 성별 검증 에러 메시지"),
                                       fieldWithPath("data.email").type(JsonFieldType.STRING).description("회원정보 이메일 검증 에러 메시지"),
                                       fieldWithPath("data.birth").type(JsonFieldType.STRING).description("회원정보 생년월일 검증 에러 메시지")
                               )
               ));
    }

    @Test
    @DisplayName("회원정보 수정 시 이미 존재하는 이메일로 회원정보 요청하면 409을 반환한다.")
    void 회원정보_수정_시_이미_존재하는_이메일로_회원정보_요청하면_409을_반환한다() throws Exception {
        UserRequest.Update 잘못된_회원가입_요청 = UserFixtures.잘못된_회원정보_수정_요청;
        CustomUserDetails principalDetails = new CustomUserDetails(UserFixtures.회원);

        mockMvc.perform(put("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(잘못된_회원가입_요청))
                                .header("Authorization", "Bearer "+AuthenticationFixtures.accessToken)
                                .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
               .andExpect(status().isBadRequest())
               .andExpectAll(
                       jsonPath("$.status").value(400),
                       jsonPath("$.message").doesNotExist(),
                       jsonPath("$.data.nickname").exists(),
                       jsonPath("$.data.birth").exists(),
                       jsonPath("$.data.email").exists(),
                       jsonPath("$.data.gender").exists()
               )
               .andDo(document(docsPath + "user-update-profile/" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("email").description("이메일"),
                                       fieldWithPath("nickname").description("닉네임"),
                                       fieldWithPath("gender").description("성별"),
                                       fieldWithPath("birth").description("생년월일 (형식: YYYY-MM-DD)")
                               ),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional(),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("회원정보 유효성 검증 실패 에러 메시지"),
                                       fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("회원정보 닉네임 검증 에러 메시지"),
                                       fieldWithPath("data.gender").type(JsonFieldType.STRING).description("회원정보 성별 검증 에러 메시지"),
                                       fieldWithPath("data.email").type(JsonFieldType.STRING).description("회원정보 이메일 검증 에러 메시지"),
                                       fieldWithPath("data.birth").type(JsonFieldType.STRING).description("회원정보 생년월일 검증 에러 메시지")
                               )
               ));
    }


    @Test
    @DisplayName("정상적인 회원 비밀번호 수정 시 200을 반환한다.")
    void 정상적인_회원_비밀번호_수정_시_200을_반환한다() throws Exception {
        UserRequest.UpdatePassword 비밀번호_변경_요청 = UserFixtures.비밀번호_변경_요청;
        doNothing().when(userService).updatePassword(any(CustomUserDetails.class),any(UserRequest.UpdatePassword.class));
        CustomUserDetails principalDetails = new CustomUserDetails(UserFixtures.회원);

        mockMvc.perform(put("/api/v1/users/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(비밀번호_변경_요청))
                                .header("Authorization", "Bearer "+AuthenticationFixtures.accessToken)
                                .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
               .andExpect(status().isOk())
               .andDo(document(docsPath + "user-update-password",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("oldPassword").description("현재 비밀번호"),
                                       fieldWithPath("newPassword").description("새로운 비밀번호"),
                                       fieldWithPath("confirmPassword").description("확인용 비밀번호")
                               )
               ));
    }

    @Test
    @DisplayName("유효성 검증에 실패하는 비밀번호 수정 요청 시 400을 반환한다.")
    void 유효성_검증에_실패하는_비밀번호_수정_요청_시_400을_반환한다() throws Exception {
        UserRequest.UpdatePassword 잘못된_회원_비밀번호_수정_요청 = UserFixtures.현재_새로운_비밀번호_동일한_비밀번호_변경_요청;
        CustomUserDetails principalDetails = new CustomUserDetails(UserFixtures.회원);

        doThrow(new UpdatePasswordFailException("현재 비밀번호와 새로운 비밀번호가 동일합니다."))
                .when(userService).updatePassword(any(CustomUserDetails.class), any(UserRequest.UpdatePassword.class));


        mockMvc.perform(put("/api/v1/users/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(잘못된_회원_비밀번호_수정_요청))
                                .header("Authorization", "Bearer "+AuthenticationFixtures.accessToken)
                                .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
               .andExpect(status().isBadRequest())
               .andExpectAll(
                       jsonPath("$.status").value(400),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               )
               .andDo(document(docsPath + "user-update-password/" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("oldPassword").description("현재 비밀번호"),
                                       fieldWithPath("newPassword").description("새로운 비밀번호"),
                                       fieldWithPath("confirmPassword").description("확인용 비밀번")
                               ),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지"),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                               )
               ));
    }

    @Test
    @DisplayName("정상적인 회원 탈퇴 시 200을 반환한다.")
    void 정상적인_회원_탈퇴_시_200을_반환한다() throws Exception {
        doNothing().when(userService).delete(any(CustomUserDetails.class));
        CustomUserDetails principalDetails = new CustomUserDetails(UserFixtures.회원);

        mockMvc.perform(delete("/api/v1/users")
                                .header("Authorization", "Bearer "+AuthenticationFixtures.accessToken)
                                .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
               .andExpect(status().isOk())
               .andDo(document(docsPath + "user-delete",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint())
               ));
    }

    @Test
    @DisplayName("존재하지 않는 회원 아이디 중복 검증 시 200을 반환한다")
    void 존재하지_않는_회원_아이디_중복_검증_시_200을_반환한다() throws Exception {
        doNothing().when(userService).checkDuplicatedUserId(any(String.class));

        mockMvc.perform(get("/api/v1/users/check-user-id")
                                .param("userId", "test"))
               .andExpect(status().isOk())
               .andDo(document(docsPath + "user-validate-userId",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               queryParameters(
                                       parameterWithName("userId").description("검증할 유저 아이디")
                               )
               ));
    }

    @Test
    @DisplayName("이미 존재하는 아이디 중복 검증 시 409를 반환한다.")
    void 이미_존재하는_아이디_중복_검증_시_409를_반환한다() throws Exception {
        doThrow(new DuplicatedUserException("존재하는 아이디입니다.")).when(userService).checkDuplicatedUserId(any(String.class));

        mockMvc.perform(get("/api/v1/users/check-user-id")
                                .param("userId", "test"))
               .andExpect(status().isConflict())
               .andExpectAll(
                       jsonPath("$.status").value(409),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data.newPassword").doesNotExist()
               )
               .andDo(document(docsPath + "user-validate-userId/" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               queryParameters(
                                       parameterWithName("userId").description("검증할 유저 아이디")
                               ),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지"),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                               )
               ));
    }



    @Test
    @DisplayName("존재하지 않는 이메일 중복 검증 시 200을 반환한다.")
    void 존재하지_않는_이메일_중복_검증_시_200을_반환한다() throws Exception {
        doNothing().when(userService).checkDuplicatedEmail(any(String.class));

        mockMvc.perform(get("/api/v1/users/check-email")
                                .param("email", "test@test.com"))
               .andExpect(status().isOk())
               .andDo(document(docsPath + "user-validate-email",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               queryParameters(
                                       parameterWithName("email").description("검증할 유저 이메일")
                               )
               ));
    }

    @Test
    @DisplayName("이미 존재하는 이메일 중복 검증 시 409을 반환한다.")
    void 이미_존재하는_이메일_중복_검증_시_409을_반환한다 () throws Exception {
        doThrow(new DuplicatedUserException("이메일 중복입니다")).when(userService).checkDuplicatedEmail(any(String.class));

        mockMvc.perform(get("/api/v1/users/check-email")
                                .param("email", "test@test.com"))
               .andExpect(status().isConflict())
               .andExpectAll(
                       jsonPath("$.status").value(409),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data.newPassword").doesNotExist()
               )
               .andDo(document(docsPath + "user-validate-email/" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).optional().description("응답 메시지"),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                               )
               ));
    }
}

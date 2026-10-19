package com.gerard.jobapplicationtracker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JobApplicationOwnershipTest {

    private static final String BASE = "/api/v1/tracker/";
    private static final String AUTH = "/api/v1/tracker/auth/";

    @Autowired
    private MockMvc mockMvc;

    private String tokenFor(String email) throws Exception {
        String creds = """
                {"email":"%s","password":"password123"}""".formatted(email);
        mockMvc.perform(post(AUTH + "sign-up")
                        .contentType(MediaType.APPLICATION_JSON).content(creds))
                .andExpect(status().isCreated());
        String body = mockMvc.perform(post(AUTH + "sign-in")
                        .contentType(MediaType.APPLICATION_JSON).content(creds))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private void createApplication(String token, String company) throws Exception {
        String json = """
                {"position":"Dev","company":"%s","jobType":"FULL_TIME","workMode":"REMOTE"}"""
                .formatted(company);
        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void listWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    void eachUserSeesOnlyTheirOwnApplications() throws Exception {
        String tokenA = tokenFor("a@test.com");
        String tokenB = tokenFor("b@test.com");
        createApplication(tokenA, "Acme");
        createApplication(tokenA, "Globex");
        createApplication(tokenB, "Initech");

        mockMvc.perform(get(BASE).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user").doesNotExist());

        mockMvc.perform(get(BASE).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void missingStatusDefaultsToSaved() throws Exception {
        String token = tokenFor("c@test.com");
        String json = """
                {"position":"Dev","company":"Acme","jobType":"FULL_TIME","workMode":"REMOTE"}""";
        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SAVED"));
    }

    @Test
    void createWithoutPositionIsBadRequest() throws Exception {
        String token = tokenFor("d@test.com");
        String json = """
                {"company":"Acme","jobType":"FULL_TIME","workMode":"REMOTE"}""";
        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    private String createAndGetId(String token, String company) throws Exception {
        String json = """
            {"position":"Dev","company":"%s","jobType":"FULL_TIME","workMode":"REMOTE"}"""
                .formatted(company);
        String body = mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void otherUserCannotReadUpdateOrDelete() throws Exception {
        String tokenA = tokenFor("a2@test.com");
        String tokenB = tokenFor("b2@test.com");
        String id = createAndGetId(tokenA, "Acme");

        mockMvc.perform(get(BASE + id).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch(BASE + id + "/status")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put(BASE + id)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\":\"X\",\"company\":\"Y\",\"jobType\":\"FULL_TIME\",\"workMode\":\"REMOTE\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(BASE + id).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // untouched for the owner
        mockMvc.perform(get(BASE + id).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Acme"))
                .andExpect(jsonPath("$.status").value("SAVED"));
    }

    @Test
    void ownerCanUpdateEditAndDelete() throws Exception {
        String token = tokenFor("e@test.com");
        String id = createAndGetId(token, "Acme");

        mockMvc.perform(patch(BASE + id + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INTERVIEW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEW"));

        mockMvc.perform(put(BASE + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\":\"Senior Dev\",\"company\":\"Acme Ltd\",\"jobType\":\"CONTRACT\",\"workMode\":\"HYBRID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Acme Ltd"))
                .andExpect(jsonPath("$.status").value("INTERVIEW"));

        mockMvc.perform(delete(BASE + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(BASE + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
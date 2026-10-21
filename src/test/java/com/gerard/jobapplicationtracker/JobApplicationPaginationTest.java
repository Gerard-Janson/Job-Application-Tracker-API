package com.gerard.jobapplicationtracker;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class JobApplicationPaginationTest {

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

    private void createMany(String token, int count) throws Exception {
        for (int i = 1; i <= count; i++) {
            createApplication(token, "Company-" + i);
        }
    }

    @Test
    void firstPageReturnsRequestedSizeAndTotals() throws Exception {
        String token = tokenFor("page1@test.com");
        createMany(token, 5);

        mockMvc.perform(get(BASE + "?page=0&size=2").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(3));
    }

    @Test
    void lastPageReturnsTheRemainder() throws Exception {
        String token = tokenFor("page2@test.com");
        createMany(token, 5);

        mockMvc.perform(get(BASE + "?page=2&size=2").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.number").value(2));
    }

    @Test
    void pageBeyondTheLastReturnsEmptyContent() throws Exception {
        String token = tokenFor("page3@test.com");
        createMany(token, 5);

        mockMvc.perform(get(BASE + "?page=99&size=2").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(5));
    }

    @Test
    void defaultPageSizeIsTen() throws Exception {
        String token = tokenFor("page4@test.com");
        createMany(token, 12);

        mockMvc.perform(get(BASE).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(12))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    void sortByCompanyAscendingOrdersResults() throws Exception {
        String token = tokenFor("sort1@test.com");
        createApplication(token, "Charlie");
        createApplication(token, "Alpha");
        createApplication(token, "Bravo");

        mockMvc.perform(get(BASE + "?sort=company,asc").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].company").value(contains("Alpha", "Bravo", "Charlie")));
    }

    @Test
    void sortByCompanyDescendingOrdersResults() throws Exception {
        String token = tokenFor("sort2@test.com");
        createApplication(token, "Charlie");
        createApplication(token, "Alpha");
        createApplication(token, "Bravo");

        mockMvc.perform(get(BASE + "?sort=company,desc").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].company").value(contains("Charlie", "Bravo", "Alpha")));
    }

    @Test
    void pagesOnlyContainTheCallersOwnApplications() throws Exception {
        String tokenA = tokenFor("owner-a@test.com");
        String tokenB = tokenFor("owner-b@test.com");
        for (int i = 1; i <= 3; i++) {
            createApplication(tokenA, "A-" + i);
        }
        for (int i = 1; i <= 2; i++) {
            createApplication(tokenB, "B-" + i);
        }

        mockMvc.perform(get(BASE + "?size=10").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].company").value(everyItem(startsWith("B-"))));

        mockMvc.perform(get(BASE + "?size=10").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].company").value(everyItem(startsWith("A-"))));
    }
}
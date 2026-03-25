package com.castcanvaslab.api.workspace.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.castcanvaslab.api.auth.infrastructure.JwtTokenProvider;
import com.castcanvaslab.api.config.TestRedisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class WorkspaceControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private String signupAndGetToken(String email, String nickname) throws Exception {
        Map<String, String> signupRequest =
                Map.of("email", email, "password", "password123", "nickname", nickname);

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andReturn();

        String userId =
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .get("data")
                        .get("id")
                        .asText();

        return jwtTokenProvider.createAccessToken(UUID.fromString(userId));
    }

    private String createWorkspaceAndGetId(String token, String name, String description)
            throws Exception {
        Map<String, String> request = Map.of("name", name, "description", description);

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/workspaces")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andReturn();

        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("id")
                .asText();
    }

    @Test
    void createWorkspaceReturnsCreated() throws Exception {
        String token = signupAndGetToken("ws.create@example.com", "creator");
        Map<String, String> request =
                Map.of("name", "My Workspace", "description", "A test workspace");

        mockMvc.perform(
                        post("/api/v1/workspaces")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("My Workspace"))
                .andExpect(jsonPath("$.data.description").value("A test workspace"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void createWorkspaceWithoutTokenReturnsForbidden() throws Exception {
        Map<String, String> request = Map.of("name", "My Workspace", "description", "desc");

        mockMvc.perform(
                        post("/api/v1/workspaces")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createWorkspaceWithBlankNameReturnsBadRequest() throws Exception {
        String token = signupAndGetToken("ws.blank@example.com", "blankuser");
        Map<String, String> request = Map.of("name", "", "description", "desc");

        mockMvc.perform(
                        post("/api/v1/workspaces")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyWorkspacesReturnsOwnedWorkspaces() throws Exception {
        String token = signupAndGetToken("ws.list@example.com", "listuser");
        createWorkspaceAndGetId(token, "Workspace A", "desc A");
        createWorkspaceAndGetId(token, "Workspace B", "desc B");

        mockMvc.perform(get("/api/v1/workspaces").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getWorkspaceByIdReturnsWorkspace() throws Exception {
        String token = signupAndGetToken("ws.get@example.com", "getuser");
        String workspaceId = createWorkspaceAndGetId(token, "Get Workspace", "desc");

        mockMvc.perform(
                        get("/api/v1/workspaces/" + workspaceId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(workspaceId))
                .andExpect(jsonPath("$.data.name").value("Get Workspace"));
    }

    @Test
    void getWorkspaceByIdNotFoundReturns404() throws Exception {
        String token = signupAndGetToken("ws.notfound@example.com", "notfounduser");

        mockMvc.perform(
                        get("/api/v1/workspaces/" + UUID.randomUUID())
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WORKSPACE_404"));
    }

    @Test
    void getWorkspaceByIdForbiddenForOtherUser() throws Exception {
        String ownerToken = signupAndGetToken("ws.owner@example.com", "owner");
        String otherToken = signupAndGetToken("ws.other@example.com", "other");
        String workspaceId = createWorkspaceAndGetId(ownerToken, "Owner Workspace", "desc");

        mockMvc.perform(
                        get("/api/v1/workspaces/" + workspaceId)
                                .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("WORKSPACE_403"));
    }

    @Test
    void updateWorkspaceReturnsUpdated() throws Exception {
        String token = signupAndGetToken("ws.update@example.com", "updateuser");
        String workspaceId = createWorkspaceAndGetId(token, "Old Name", "old desc");

        Map<String, String> updateRequest = Map.of("name", "New Name", "description", "new desc");

        mockMvc.perform(
                        put("/api/v1/workspaces/" + workspaceId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.description").value("new desc"));
    }

    @Test
    void updateWorkspaceForbiddenForOtherUser() throws Exception {
        String ownerToken = signupAndGetToken("ws.upowner@example.com", "upowner");
        String otherToken = signupAndGetToken("ws.upother@example.com", "upother");
        String workspaceId = createWorkspaceAndGetId(ownerToken, "Owner WS", "desc");

        Map<String, String> updateRequest = Map.of("name", "Hacked", "description", "hacked");

        mockMvc.perform(
                        put("/api/v1/workspaces/" + workspaceId)
                                .header("Authorization", "Bearer " + otherToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("WORKSPACE_403"));
    }

    @Test
    void deleteWorkspaceReturnsOk() throws Exception {
        String token = signupAndGetToken("ws.delete@example.com", "deleteuser");
        String workspaceId = createWorkspaceAndGetId(token, "To Delete", "desc");

        mockMvc.perform(
                        delete("/api/v1/workspaces/" + workspaceId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        mockMvc.perform(
                        get("/api/v1/workspaces/" + workspaceId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWorkspaceForbiddenForOtherUser() throws Exception {
        String ownerToken = signupAndGetToken("ws.delowner@example.com", "delowner");
        String otherToken = signupAndGetToken("ws.delother@example.com", "delother");
        String workspaceId = createWorkspaceAndGetId(ownerToken, "Protected WS", "desc");

        mockMvc.perform(
                        delete("/api/v1/workspaces/" + workspaceId)
                                .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("WORKSPACE_403"));
    }
}

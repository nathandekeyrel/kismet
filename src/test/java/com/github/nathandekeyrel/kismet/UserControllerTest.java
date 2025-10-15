package com.github.nathandekeyrel.kismet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 1. Use @SpringBootTest to load the ENTIRE application context.
@SpringBootTest
// 2. Add @AutoConfigureMockMvc to get a configured MockMvc instance.
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 3. We ONLY need to mock the repository now. The other beans
    // (PasswordEncoder, JpaUserDetailsService) will be the REAL ones created by Spring.
    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser // This annotation works perfectly with @SpringBootTest too
    void whenAuthenticated_thenReturnsHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Welcome to Kismet!")));
    }

    @Test
    void whenUnauthenticated_thenRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                // Now, this test will correctly check the behavior of the REAL security chain
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}
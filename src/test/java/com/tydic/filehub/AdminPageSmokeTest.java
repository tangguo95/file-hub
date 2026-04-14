package com.tydic.filehub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPageSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void jobsPageLoads() throws Exception {
        mockMvc.perform(get("/admin/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void filesPageLoads() throws Exception {
        mockMvc.perform(get("/admin/files"))
                .andExpect(status().isOk());
    }

    @Test
    void datasourcesPageLoads() throws Exception {
        mockMvc.perform(get("/admin/datasources"))
                .andExpect(status().isOk());
    }
}

package com.urjcservice.backend.rest;

import com.urjcservice.backend.dtos.DashboardStatsDTO;
import com.urjcservice.backend.service.StatsService;
import com.urjcservice.backend.security.jwt.JwtTokenProvider; 
import org.springframework.security.core.userdetails.UserDetailsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsRestController.class)
@AutoConfigureMockMvc(addFilters = false) //Desactivate filters for avoid using tokens and prove the real functionality of the file
public class StatsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

   
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;
    

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetDashboardStatsDefaultDate() throws Exception {
        DashboardStatsDTO mockStats = new DashboardStatsDTO();
        mockStats.setTotalRooms(10);
        mockStats.setOccupiedPercentage(50.0);
        mockStats.setHourlyOccupancy(new HashMap<>());

        given(statsService.getStats(any(LocalDate.class))).willReturn(mockStats);

        mockMvc.perform(get("/api/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms").value(10))
                .andExpect(jsonPath("$.occupiedPercentage").value(50.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetDashboardStatsSpecificDate() throws Exception {
        DashboardStatsDTO mockStats = new DashboardStatsDTO();
        mockStats.setOccupiedPercentage(75.0);

        LocalDate specificDate = LocalDate.of(2025, 12, 25);
        given(statsService.getStats(specificDate)).willReturn(mockStats);

        mockMvc.perform(get("/api/stats")
                .param("date", "2025-12-25")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupiedPercentage").value(75.0));
    }
}
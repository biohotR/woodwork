package woodwork.category;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import woodwork.security.CustomUserDetailsService;
import woodwork.security.JwtAuthenticationFilter;

// @WebMvcTest boots up only the web layer(Controllers) - saves memory
@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false) // to bypass the jwt security filter for this specific test
class CategoryControllerTest {

    @Autowired
    // the fake web browser
    private MockMvc mockMvc;

    // converts java objects to json strings
    private ObjectMapper objectMapper = new ObjectMapper();
    
    // @MockitoBean is like @Mock
    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void createCategory_ShouldReturnSavedCategoryAnd200Ok() throws Exception {
        // set up the mock service response
        CategoryDto inputDto = new CategoryDto();
        inputDto.setName("Hardware");

        CategoryDto returnedDto = new CategoryDto();
        returnedDto.setId(UUID.randomUUID());
        returnedDto.setName("Hardware");

        when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(returnedDto);

        // simulate a real post request
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto))) // simulates the frontend sending json
                
               // asserts the status code
               .andExpect(status().isOk())
               
               // asserts the json body sent back to the user
               .andExpect(jsonPath("$.name").value("Hardware"))
               .andExpect(jsonPath("$.id").exists());
    }
}

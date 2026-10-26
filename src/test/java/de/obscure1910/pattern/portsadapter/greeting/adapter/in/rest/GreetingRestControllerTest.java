package de.obscure1910.pattern.portsadapter.greeting.adapter.in.rest;

import de.obscure1910.pattern.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GreetingRestController.class)
public class GreetingRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SayHello sayHello;

    @Test
    void shouldReturnGreeting() throws Exception {
        when(sayHello.sayHello(new User("Alice")))
                .thenReturn("Herzlich willkommen zum ersten Mal, Alice!");

        mockMvc.perform(get("/greeting").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Herzlich willkommen zum ersten Mal, Alice!"));
    }

    @Test
    void shouldAskForNameWhenQueryParameterIsMissing() throws Exception {
        mockMvc.perform(get("/greeting"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Wie heißt die zu grüßende Person?"));

        verifyNoInteractions(sayHello);
    }

    @Test
    void shouldAskForNameWhenQueryParameterIsBlank() throws Exception {
        mockMvc.perform(get("/greeting").param("name", "   "))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Wie heißt die zu grüßende Person?"));

        verifyNoInteractions(sayHello);
    }

}

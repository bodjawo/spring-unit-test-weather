package com.teamtreehouse.web.controller;

import static org.junit.Assert.*;

import com.teamtreehouse.domain.Favorite;
import com.teamtreehouse.service.FavoriteNotFoundException;
import com.teamtreehouse.service.FavoriteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import static com.teamtreehouse.domain.Favorite.FavoriteBuilder;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class FavoriteControllerTest {
    private MockMvc mockMvc;
    
    @InjectMocks    // Create instance of a favorite controller
    private FavoriteController controller;
    
    @Mock
    private FavoriteService service;
    
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }
    
    @Test
    public void index_ShouldIncludeFavoritesInModel() throws Exception {
        // Arrange the mock behaviour
        List<Favorite> favorites = Arrays.asList(
            new FavoriteBuilder(1L).withAddress("Chicago").withPlaceId("chicago1").build(),
            new FavoriteBuilder(2L).withAddress("Omaha").withPlaceId("omaha1").build()
        );
        
        // Act (perform the MVC request
        when(service.findAll()).thenReturn(favorites);
        
        // Assert results
        mockMvc.perform(get("/favorites"))
            .andExpect(status().isOk())
            .andExpect(view().name("favorite/index"))
            .andExpect(model().attribute("favorites", favorites));
        
        // Assertion verification
        verify(service).findAll();
    }
    
    @Test
    public void add_ShouldRedirectToNewFavorite() throws Exception {
        // Arrange the mock behaviour
        doAnswer(invocation -> {
            Favorite favorite = (Favorite)invocation.getArguments()[0];
            favorite.setId(1L);
            return null;
        }).when(service).save(any(Favorite.class));
    
        // Act (perform the MVC request and assert results
        mockMvc.perform(
            post("/favorites")
                .param("formattedAddress", "chicago, il")
                    .param("placeId", "windycity")
        ).andExpect(redirectedUrl("/favorites/1"));
    
        // Assertion verification
        verify(service).save(any(Favorite.class));
    }
    
    @Test
    public void detail_ShouldErrorOnNotFound() throws Exception {
        // Arrange the mock behaviour
        when(service.findById(1L)).thenThrow(FavoriteNotFoundException.class);
        
        // Act (perform the MVC request
        mockMvc.perform(get("/favorites/1"))
            .andExpect(view().name("error"))
                .andExpect(model().attribute("ex", org.hamcrest.Matchers.instanceOf(FavoriteNotFoundException.class)));
        // Assert results
        verify(service).findById(1L);
    }
}
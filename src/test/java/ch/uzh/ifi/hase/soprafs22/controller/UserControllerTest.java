package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network. This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  //  @MockBean private UserRepository userRepository;

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setUsername("xiongli");
    user.setPassword("password");
    user.setLogged_in(true);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest =
        get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc
        .perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        //        .andExpect(jsonPath("$[0].password", is(user.getPassword())))
        .andExpect(jsonPath("$[0].logged_in", is(user.getLogged_in())));
  }

  @Test
  public void userPost201() throws Exception {
    // given
    User user = new User();
    user.setId(999L);
    user.setUsername("testUsername");
    user.setPassword("testPassword");
    user.setLogged_in(true);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("testPassword");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/users").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPostDTO));

    // then
    mockMvc
        .perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        //        .andExpect(jsonPath("$.password", is(user.getPassword())))
        .andExpect(jsonPath("$.logged_in", is(user.getLogged_in())));
  }

  @Test
  void userPost409() throws Exception {
    User user = new User();
    user.setId(999L);
    user.setUsername("testUsername");
    user.setPassword("testPassword");
    //      user.setLogged_in(true);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");
    userPostDTO.setPassword("testPassword");

    given(userService.createUser(Mockito.any()))
        .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "username already exists"));

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest =
        post("/users").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPostDTO));

    mockMvc.perform(postRequest).andExpect(status().isConflict());
  }

  @Test
  public void userget200() throws Exception {
    // given
    User user = new User();
    // user.setName("Firstname Lastname");
    user.setUsername("TestName");
    user.setPassword("Password");
    user.setLogged_in(true);
    user.setId(1L);

    User user1 = new User();
    // user.setName("Firstname Lastname");
    user1.setUsername("TestName1");
    user1.setPassword("Password1");
    user1.setLogged_in(true);
    user1.setId(2L);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.findById(1L)).willReturn(user);
    given(userService.findById(2L)).willReturn(user1);
    // when
    MockHttpServletRequestBuilder getRequest1 =
        get("/users/" + user.getId()).contentType(MediaType.APPLICATION_JSON);
    MockHttpServletRequestBuilder getRequest2 =
        get("/users/" + user1.getId()).contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc
        .perform(getRequest1)
        .andExpect(status().isOk())
        // .andExpect(jsonPath("$[0].name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.logged_in", is(user.getLogged_in())));
    // then
    mockMvc
        .perform(getRequest2)
        .andExpect(status().isOk())
        // .andExpect(jsonPath("$", hasSize(1)))
        // .andExpect(jsonPath("$[0].name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user1.getUsername())))
        .andExpect(jsonPath("$.logged_in", is(user1.getLogged_in())));
  }

  @Test
  void userGet404() throws Exception {
    Long userId = 1L;
    given(userService.findById(userId))
        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

    MockHttpServletRequestBuilder getRequest =
        get("/users/1").contentType(MediaType.APPLICATION_JSON);

    this.mockMvc.perform(getRequest).andExpect(status().isNotFound());
  }

    @Test
    void userGet401() throws Exception {
        Long userId = 1L;
        given(userService.findById(userId))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first"));

        MockHttpServletRequestBuilder getRequest =
                get("/users/1").contentType(MediaType.APPLICATION_JSON);

        this.mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
    }


    @Test
    void UserPut204() throws Exception {
        Long userId = 1L;
        User storedUser = new User();
        storedUser.setUsername("username");
        storedUser.setPassword("password");
        storedUser.setId(1L);
        given(userService.findById(userId))
                .willReturn(storedUser);

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUsername");
        userPutDTO.setToken("username");

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest =
                put("/users/"+userId).contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPutDTO));
        mockMvc.perform(putRequest).andExpect(status().isNoContent());
    }

    @Test
    void userPut404() throws Exception {
      Long userId = 1L;
        given(userService.findById(userId))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,"user not found"));

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUsername");
        userPutDTO.setToken("username");

        given(userService.createUser(Mockito.any()))
                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "username already exists"));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest =
                put("/users/"+userId).contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPutDTO));

        mockMvc.perform(putRequest).andExpect(status().isNotFound());
    }

    @Test
    void userPut401() throws Exception {
        Long userId = 1L;
        User storedUser = new User();
        storedUser.setUsername("username");
        storedUser.setPassword("password");
        storedUser.setId(1L);
        given(userService.findById(userId))
                .willReturn(storedUser);

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUsername");
        userPutDTO.setToken("notRespondingUsername");

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest =
                put("/users/"+userId).contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPutDTO));

        mockMvc.perform(putRequest).andExpect(status().isUnauthorized());
    }





  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   *
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}

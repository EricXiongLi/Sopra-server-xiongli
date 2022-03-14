package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.TokenDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * User Controller This class is responsible for handling all REST request that are related to the
 * user. The controller will receive the request and delegate the execution to the UserService and
 * finally return the result.
 */
@RestController
@CrossOrigin
public class UserController {

  private final UserService userService;

  @Autowired
  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/user/login")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String login(HttpSession httpSession, @RequestBody User user) {
    User foundUser = userService.findByUser(user);

    if (foundUser != null) {
      if (foundUser.getPassword().equals(user.getPassword())) {
        foundUser.setLogged_in(true);
        userService.updateUser(foundUser);
        httpSession.setAttribute("user", foundUser);
        return foundUser.getUsername();
      } else {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
      }
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
    }
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO addUser(@RequestBody UserPostDTO userPostDTO) {
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
    User createdUser = userService.createUser(userInput);
    createdUser.setLogged_in(true);
    createdUser.setToken(userPostDTO.getUsername());
    userService.updateUser(createdUser);
    //    httpSession.setAttribute("user", createdUser);
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @GetMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO getUserById(@PathVariable("userId") Long userId) {
    //    User storedUser = (User) httpSession.getAttribute("user");
    //    if (storedUser == null) {
    //      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
    //    }
    User user = userService.findById(userId);
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

  @PutMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateUser(
      HttpSession httpSession,
      @PathVariable("userId") Long userId,
      @RequestBody UserPutDTO userPutDTO) {
      User user = userService.findById(userId);
    //      User storedUser = (User) httpSession.getAttribute("user");
    //    if (storedUser == null || !Objects.equals(storedUser.getId(), userId)) {
    //      throw new ResponseStatusException(
    //          HttpStatus.UNAUTHORIZED,
    //          "You are not authorized to do this, Please log in this account first");
    //    }
    String currentToken = userPutDTO.getToken();
    if (currentToken == null || !currentToken.equals(user.getUsername())) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED,
          "You are not authorized to do this, Please log in this account first");
    }
    user.setUsername(userPutDTO.getUsername());
    user.setBirthday(userPutDTO.getBirthday());
    userService.updateUser(user);
  }

  @PostMapping("/user/logout")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public void logout(@RequestBody TokenDTO userLogoutDTO) {
    //    User currentUser = (User)httpSession.getAttribute("user");
    //    Long currentUserId = currentUser.getId();
    //    User newUser = userService.findById(currentUserId);
    //    newUser.setLogged_in(false);
    //    userService.updateUser(newUser);
    //    httpSession.removeAttribute("user");
    User currentUser = userService.findByUsername(userLogoutDTO.getToken());
    currentUser.setLogged_in(false);
    userService.updateUser(currentUser);
  }
}

package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
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
import java.util.Objects;

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
  public void login(HttpSession httpSession, @RequestBody User user) {
    User foundUser = userService.findByUsername(user);
    if (foundUser != null) {
      if (foundUser.getPassword().equals(user.getPassword())) {
        httpSession.setAttribute("user", foundUser);
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
  public UserGetDTO addUser(HttpSession httpSession, @RequestBody UserPostDTO userPostDTO) {
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
    User createdUser = userService.createUser(userInput);
    httpSession.setAttribute("user", createdUser);
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @GetMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO getUserById(HttpSession httpSession, @PathVariable("userId") Long userId) {
    User storedUser = (User) httpSession.getAttribute("user");
    if (storedUser == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
    }
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
    User storedUser = (User) httpSession.getAttribute("user");
    if (storedUser== null || !Objects.equals(storedUser.getId(), userId)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to do this, Please log in this account first");
    }
    User user = userService.findById(userId);
    user.setUsername(userPutDTO.getUsername());
    user.setBirthday(userPutDTO.getBirthday());
    userService.updateUser(user);
  }

  @PostMapping("/user/logout")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public void logout(HttpSession httpSession) {
    httpSession.removeAttribute("user");
  }
}
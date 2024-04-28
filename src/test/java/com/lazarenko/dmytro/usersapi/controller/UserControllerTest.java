package com.lazarenko.dmytro.usersapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lazarenko.dmytro.usersapi.exception.InvalidDateRangeException;
import com.lazarenko.dmytro.usersapi.exception.MinimumAgeRequirementException;
import com.lazarenko.dmytro.usersapi.exception.UserNotFoundException;
import com.lazarenko.dmytro.usersapi.model.User;
import com.lazarenko.dmytro.usersapi.model.assembler.UserModelAssembler;
import com.lazarenko.dmytro.usersapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests the web layer operations managed by {@link UserController}.
 *
 * @author Dmytro Lazarenko
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserModelAssembler userModelAssembler;

    private User user;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));

        EntityModel<User> entityModel = EntityModel.of(user);
        entityModel.add(WebMvcLinkBuilder.linkTo(methodOn(UserController.class).one(user.getEmail())).withSelfRel());

        given(userModelAssembler.toModel(any(User.class))).willReturn(entityModel);
    }

    @Test
    void testNewUser() throws Exception {
        given(userService.addUser(any(User.class))).willReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + user.getEmail().replace("@", "%40")));
    }

    @Test
    void testAllUsers() throws Exception {
        List<User> users = List.of(user);

        given(userService.findAllUsers()).willReturn(users);

        mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userList[0].email", is(user.getEmail())));
    }

    @Test
    void testGetUserByEmail_Success() throws Exception {
        given(userService.findUserByEmail(any(String.class))).willReturn(user);

        mockMvc.perform(get("/users/{email}", user.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void testGetUserByEmail_NotFound() throws Exception {
        given(userService.findUserByEmail(any(String.class))).willThrow(new UserNotFoundException(user.getEmail()));

        mockMvc.perform(get("/users/{email}", user.getEmail()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(UserNotFoundException.class, result.getResolvedException()))
                .andExpect(jsonPath("$.message").value("Could not find user " + user.getEmail()));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        user.setFirstName("Updated Name");
        given(userService.updateUser(eq(user.getEmail()), anyMap())).willReturn(user);

        mockMvc.perform(patch("/users/{email}", user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Updated Name\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated Name")));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        mockMvc.perform(delete("/users/{email}", user.getEmail()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        doThrow(new UserNotFoundException(user.getEmail())).when(userService).deleteUser(user.getEmail());

        mockMvc.perform(delete("/users/{email}", user.getEmail()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Could not find user " + user.getEmail()));
    }

    @Test
    void testCreateUser_FailsMinimumAgeRequirement() throws Exception {
        given(userService.addUser(any(User.class))).willThrow(new MinimumAgeRequirementException(18));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User must be at least 18 years old"));
    }

    @Test
    void testUsersByBirthDateRange_Success() throws Exception {
        List<User> users = List.of(user);
        LocalDate from = LocalDate.of(1990, 1, 1);
        LocalDate to = LocalDate.of(2000, 12, 31);
        given(userService.findUsersByBirthDateRange(from, to)).willReturn(users);

        mockMvc.perform(get("/users/by-birthdate-range")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userList[0].email", is(user.getEmail())));
    }

    @Test
    void testUsersByBirthDateRange_InvalidDateRange() throws Exception {
        LocalDate from = LocalDate.of(2001, 1, 1);
        LocalDate to = LocalDate.of(2000, 12, 31);
        given(userService.findUsersByBirthDateRange(from, to)).willThrow(new InvalidDateRangeException());

        mockMvc.perform(get("/users/by-birthdate-range")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The 'from' date must be before the 'to' date"));
    }

    @Test
    void testReplaceUser_Success() throws Exception {
        given(userService.replaceUser(eq(user.getEmail()), any(User.class))).willReturn(user);

        mockMvc.perform(put("/users/{email}", user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())));
    }

    @Test
    void testReplaceUser_NotFoundCreatesUser() throws Exception {
        given(userService.replaceUser(eq("new@test.com"), any(User.class))).willReturn(user);

        mockMvc.perform(put("/users/{email}", "new@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }
}

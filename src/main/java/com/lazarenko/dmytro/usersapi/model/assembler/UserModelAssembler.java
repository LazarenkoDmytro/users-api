package com.lazarenko.dmytro.usersapi.model.assembler;

import com.lazarenko.dmytro.usersapi.controller.UserController;
import com.lazarenko.dmytro.usersapi.model.User;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembles User models into HATEOAS-compliant EntityModel instances.
 *
 * @author Dmytro Lazarenko
 */
@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

    @NonNull
    @Override
    public EntityModel<User> toModel(@NonNull User user) {

        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).one(user.getEmail())).withSelfRel(),
                linkTo(methodOn(UserController.class).all()).withRel("users"),
                linkTo(methodOn(UserController.class).updateUser(user.getEmail(), new HashMap<>())).withRel("update"),
                linkTo(methodOn(UserController.class).replaceUser(user.getEmail(), new User())).withRel("replace"),
                linkTo(methodOn(UserController.class).deleteUser(user.getEmail())).withRel("delete"));
    }
}

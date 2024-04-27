package com.lazarenko.dmytro.usersapi.model.assembler;

import com.lazarenko.dmytro.usersapi.controller.UserController;
import com.lazarenko.dmytro.usersapi.model.User;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

    @NonNull
    @Override
    public EntityModel<User> toModel(@NonNull User user) {

        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).one(user.getEmail())).withSelfRel(),
                linkTo(methodOn(UserController.class).all()).withRel("users"));
    }
}

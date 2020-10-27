package com.mattmalec.pterodactyl4j.application.entities.impl;

import com.mattmalec.pterodactyl4j.PteroAction;
import com.mattmalec.pterodactyl4j.application.entities.User;
import com.mattmalec.pterodactyl4j.application.managers.UserAction;
import com.mattmalec.pterodactyl4j.requests.Requester;
import com.mattmalec.pterodactyl4j.requests.Route;
import org.json.JSONObject;

public class EditUserImpl implements UserAction {

    private final Requester requester;
    private final User user;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String password;

    public EditUserImpl(User user, Requester requester) {
        this.user = user;
        this.requester = requester;
    }

    @Override
    public UserAction setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public UserAction setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public UserAction setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    @Override
    public UserAction setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    @Override
    public UserAction setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public PteroAction<User> build() {
        JSONObject json = new JSONObject();
        if (this.userName == null)
            json.put("username", this.user.getUserName());
        else
            json.put("username", this.userName);
        if (this.email == null)
            json.put("email", this.user.getEmail());
        else
            json.put("email", this.email);
        if (this.firstName == null)
            json.put("first_name", this.user.getFirstName());
        else
            json.put("first_name", this.firstName);
        if (this.lastName == null)
            json.put("last_name", this.user.getLastName());
        else
            json.put("last_name", this.lastName);
        json.put("password", this.password);
        return new PteroAction<User>() {
            final Route.CompiledRoute route = Route.Users.EDIT_USER.compile(user.getId()).withJSONdata(json);
            final JSONObject jsonObject = requester.request(route).toJSONObject();

            @Override
            public User execute() {
                return new UserImpl(jsonObject, requester);
            }
        };
    }
}

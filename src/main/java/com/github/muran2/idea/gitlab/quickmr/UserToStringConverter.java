package com.github.muran2.idea.gitlab.quickmr;

import com.github.muran2.idea.gitlab.User;
import com.intellij.util.Function;
import org.jdesktop.swingx.renderer.StringValue;

public class UserToStringConverter implements StringValue, Function<Object, String> {
    @Override
    public String getString(Object o) {
        if (o instanceof User) {
            User user = (User) o;
            StringBuilder resultString = new StringBuilder();
            if (user.getName() != null) {
                resultString.append(user.getName());
            } else {
                resultString.append(user.getId());
            }
            String username = user.getUsername();
            if (username != null && !username.trim().isEmpty()) {
                resultString.append(" (").append(username).append(")");
            }
            return resultString.toString();
        }
        return o.toString();
    }

    @Override
    public String fun(Object o) {
        return getString(o);
    }
}

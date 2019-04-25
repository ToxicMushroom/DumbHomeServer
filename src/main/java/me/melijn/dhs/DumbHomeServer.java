package me.melijn.dhs;

import org.jooby.Jooby;

public class DumbHomeServer {

    public static void main(String[] args) {
        Jooby.run(Application::new, args);
    }
}

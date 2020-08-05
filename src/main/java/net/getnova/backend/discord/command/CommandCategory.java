package net.getnova.backend.discord.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CommandCategory {

    GENERAL("General"),
    ADMIN("Admin"),
    MUSIC("Music");

    private final String name;
}

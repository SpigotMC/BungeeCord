package net.md_5.bungee.module;

import lombok.Data;

import java.io.File;

@Data
public class ModuleSpec {

    private final String name;
    private final File file;
    private final ModuleSource provider;
}

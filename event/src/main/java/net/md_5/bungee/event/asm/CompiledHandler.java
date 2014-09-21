package net.md_5.bungee.event.asm;

/**
 * Base class which represents a handler that has been compiled for execution.
 * It is included as an interface so that it may be called via native bytecode
 * instructions rather than slower reflection.
 */
public interface CompiledHandler extends Runnable
{
}

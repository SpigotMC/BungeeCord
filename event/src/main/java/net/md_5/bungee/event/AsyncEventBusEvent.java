package net.md_5.bungee.event;

public interface AsyncEventBusEvent extends EventBusEvent
{

    void onComplete();

    void registerIntent();

    boolean isRegisteredIntent();

    void completeIntent();

    void setAsyncEventContext(AsyncEventContext<?> asyncEventContext);

}

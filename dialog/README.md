BungeeCord-Dialog
=================

Highly experimental API, subject to breakage. All contributions welcome, including major refactors/design changes.

Sample Plugin
-------------

```java
    private class TestCommand extends Command
    {

        public TestCommand()
        {
            super( "btest" );
        }

        @Override
        public void execute(CommandSender sender, String[] args)
        {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            Dialog notice = new NoticeDialog( new DialogBase( new ComponentBuilder( "Hello" ).color( ChatColor.RED ).build() ) );
            player.showDialog( notice );

            notice = new NoticeDialog(
                    new DialogBase( new ComponentBuilder( "Hello" ).color( ChatColor.RED ).build() )
                            .inputs(
                                    Arrays.asList( new TextInput( "first", new ComponentBuilder( "First" ).build() ),
                                            new TextInput( "second", new ComponentBuilder( "Second" ).build() )
                                    )
                            ) )
                    .action( new ActionButton( new ComponentBuilder( "Submit Button" ).build(), new CustomClickAction( "customform" ) ) );

            player.sendMessage( new ComponentBuilder( "click me" ).event( new ShowDialogClickEvent( notice ) ).build() );
        }
    }
```

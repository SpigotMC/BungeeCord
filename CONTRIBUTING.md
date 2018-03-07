# Contributing to BungeeCord

Thanks for getting involved with the project, we'd love for you to help improve our software.

* [Code of Conduct](#coc)
* [Issues and Bugs](#issue)
* [Issue Submission Guidelines](#submit-issue)
* [Pull Request Submission Guidelines](#submit-pr)
* [Codestyle](#codestyle)

## <a name="coc"></a> Code of Conduct

Help us keep BungeeCord open and inclusive. Please read and follow our [Code of Conduct][coc].

## <a name="issue"></a> Issues and Bugs

If you find a bug in the source code, you can help us by submitting an issue to our [GitHub Repository][github]. Even better, you can submit a Pull Request with a fix.

**Please see the [Submission Guidelines](#submit-issue) below.**

## <a name="submit-issue"></a> Issue Submission Guidelines
Please check our [wiki](https://www.spigotmc.org/wiki/index/) as you may find the cause of a problem has already been documented and you will be able to fix it yourself.

For issues with configuration or Spigot plugins, post on our forums at [SpigotMC](https://www.spigotmc.org/) instead.

Before you submit your issue search the archive, maybe your question was already answered. If your issue appears to be a bug, and hasn't been reported, open a new issue. Help us to maximize
the effort we can spend fixing issues and adding new features, by not reporting duplicate issues.

The "[new issue][github-new-issue]" form contains a number of prompts that you should fill out to make it easier to understand and categorize the issue. In general, providing the following information will increase the chances of your issue being dealt
with quickly.

## <a name="submit-pr"></a> Pull Request Submission Guidelines
Before you submit your pull request consider the following guidelines:

* Search [GitHub](github-pulls) for an open or closed Pull Request that relates to your submission. You don't want to duplicate effort.
* Follow our [Codestyle](#codestyle).
* If the changes affect public APIs, change or add relevant documentation.

## <a name="codestyle"></a> Codestyle

* Use parenthesis around conditional check.
  * ```var = ( boolean ) ? a : b``` instead of ```var = boolean ? a : b```
* Use spaces around operators.
  * ```count + 1``` instead of ```count+1```
* Use spaces within function parameters:
  * ```if ( test )``` instead of ```if (test)```
* Use spaces after commas (unless separated by newlines).
* Use spaces inside the curly-braces of hash literals:
  * ```{ a, b }``` instead of ```{a, b}```
* Include a single line of whitespace between methods.
* Capitalize initialisms and acronyms in names, except for the first word, which should be lower-case:
  * getURI instead of getUri
  * uriToOpen instead of URIToOpen
* Place braces associated with a control statement on the next line, indented to the same level as the control statement:
  * ```
    while ( x == y )
    {
        something();
    }
    ```

[coc]: CODE_OF_CONDUCT.md
[github]: https://github.com/SpigotMC/BungeeCord/
[github-pulls]: https://github.com/SpigotMC/BungeeCord/pulls
[github-new-issue]: https://github.com/SpigotMC/BungeeCord/issues/new

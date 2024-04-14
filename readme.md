GIT repair broken encoding
==========================

This project should help to repair broken text files by GIT history.

Sometime, when developer using different text editor, he can convert file to any encoding. After merge file it can be merge with other encoding version. That should do broren same symbols. After long time we can found that any lines on text files with special charactters may be broken many commit's ago.

You can search true text in GIT history. But when you have a large text file with many commit's it can be probably strong. For automatize this process I make this utility.

*Please, don't do this: "Tap-tap and go to product!".*


Usage
-----

    java -Xmx200m -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar git-repair-encoding.jar path_with_text_files

Programm will scan all fiels from path_with_text_files, ordered by modify time.
Compare files between.
Make output file diff.html for show how to detect line numbers.
Make output file out.txt with repair last file item. Or do not make it, if all string was good.


Build
-----

Use maven.

> cd git-repair-encoding/
> mvn package

What this utilite doing?
------------------------
You has 3 files:

file 1.txt:

    line 1. Text1.
    line 2. Text1.
    line 3. Текст строка.
    line 4. test. 

file 2.txt:

    line 1. Text1.
    (empty)
    line 2. Text1.1
    line 3. ????? ??????.
    line 4. test.
    line 5. Строка.

file 3.txt:

    line 1. Text1.1 and 1.2
    (empty)
    line 3. ????? ??????.
    line 2. Text1.1
    line 4. test.
    line 5. Строка.

You foud broken line '????? ??????' but too later, and don't want search it in previous files by hand. Start the git-repair-encoding.jar and got next file:

out.txt:

    line 1. Text1.1 and 1.2
    (empty)
    line 3. Текст строка.
    line 2. Text1.1
    line 4. test.
    line 5. Строка.



TODO list
---------
First version of "git-repair-encoding" can work only with text files in folder, without Git. Tested only on 2 languages and 2 encodings. Funute feature will be nice:

1. improvement search alhorithm: should use more strong logic. See: 
2. add more configurable options: search window size
3. add support to more encoding, languages
4. add Git repository support. See: org.eclipse.jgit library
5. add GUI

Project structure
-----------------
* Language: Java 1.8
* Build: maven
* src - source code of peoject

Using external library:

* 'apache-any23-encoding' for auto detect file encoding
* 'slf4j-simple' for control debug output console spam
* 'junit 4' for unit test

Programm package:
* types - data structure
* engine - class for compare and repair broken files
* loader - input read
* Main class - launcher

Special class for:

* loader.FileLoader - load input data from folder
* loader.GitLoader  - TODO, not implemented, for load from Git
* engine.CompareEngine      - check file sequence for diff, link lines
* engine.DiffVisualization  - show diff data in HTML form, for debug
* engine.RepairEngine       - repair broken lines, using after CompareEngine.

License
--------
(C) 2024 Author Alexey K (github.com/PlayerO1).

License GNU GPL 3.

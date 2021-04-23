# qhttotxt - QIP history (.qhf и .ahf) to .txt converter #

### Prehistory ###
At the end of the 00s and at the beginning of the 10s QIP messenger got widespread. It worked on ICQ protocol. The 
messenger had a mobile version - QIP PDA. As a difference from other messengers storing their history in text files, 
QIP stored it in internal formats: .qhf and .ahf. Moreover, QIP for PC encrypted the history as well. Besides, as most 
messengers at that time, there was no synchronization between devices and it was possible when messages from the same 
contact were in different places. And if there is a folder "Archive" with .ahf files, messages from the same contact 
will be in separate files as well.

Several programs have been developed to translate QIP history into text files, however, as of April 2021, they are 
either not available or do not work on modern OSs. Anyway, the author was not able to find a working version in the 
public domain.

### Short description ###

qhttotxt is a small Java program that allows converting QIP internal history files (.qhf and .ahf) into human-readble 
.txt files. The program is able to combine messages from different files for one contact, choosing unique ones and 
putting them in accordance with the time of sending/receiving.

Also, there are minimal options for recovering corrupted messages and files. However, the author does not have a large 
number of files for testing, so it is likely that some corrupted files will contain missing messages.

### Usage ### 
JAVA JDK version higher than 1.8 is needed to be installed. 

The simplest way:
Download /jars/qhftotxt.jar from this repository and put it into a folder with history files. Run the program without 
any keys:

`> java -jar qhttotxt.jar`

The program will find all possible files in the given folder and convert them.

Advanced options are available using parameters (can be in any order):

`{path}` - the path of the directory with files or the path to a single file. Only one path is supported. If you need 
to convert several folders, place them into one root folder. Use the `-r` switch and specify the path to root folder.

`-с` - combining messages from one contact. If the `-r` switch is provided, then the output files will be placed in 
the working directory. Only unique messages are combined, all duplicates are removed.

`-h` - short help.

`-n {nickname}` - there is no information about the owner's nickname or UIN in the history files, so it is  
impossible to restore it. If the nickname is not specified by this key, then it will be 'You' in all texts by default.

`-p {codepage}` - force setting of a codepage. If the program runs on a computer that does not have a language of 
history files, this key may help. The codepage must be specified in the standard format. See details 
https://en.wikipedia.org/wiki/Code_page.

`-r` - recursive search in subdirectories of the working directory. By default, recursive search is disabled. The 
output files will be placed in the same directories as their sources.

`-z {timezone}` - force setting of timezone for displaying message time. If the time zone is different on a computer 
where the program runs from the one where the history was recorded, then the time in output files will be displayed 
incorrectly. The time zone must be specified in a standard format. See details 
https://en.wikipedia.org/wiki/List_of_tz_database_time_zones

Examples:
Convert files in a current (the one with file `qhttotxt.jar`) Directory:

`> java -jar qhttotxt.jar`

Convert file `C:\Users\User\Documents\file.qhf` only (file `qhttotxt.jar` can be in any place):

`> java -jar qhttotxt.jar C:\Users\User\Documents\file.qhf`

Covert files in `C:\Users\User\Documents` and all subdirectories, set the nickname "John Snow" and codepage 
"windows-1251" (standart for Russian). File `qhttotxt.jar` can be in any place:

`> java -jar qhttotxt.jar -r -n "John Snow" -p "windows-1251" C:\Users\User\Documents`

Covert files in `C:\Users\User\Documents` and all subdirectories, combine histories from different files, set the 
nickname "John Snow" and codepage "windows-1251" (standart for Russian), timezone "Europe/Moscow" (full set of keys). 
File `qhttotxt.jar` can be in any place: 

`> java -jar qhttotxt.jar -c -n "John Snow" -p "windows-1251" -z "Europe/Moscow" -r C:\Users\User\Documents`


## QHF (AHF) format description ##

Original format description was taken from https://github.com/MolinRE/QIParser. Without this description, this program 
would not exist. Many thanks to the author and owner Konstantin Komarov (https://github.com/MolinRE) for permission 
for using it in this project as well. There is another description 
https://alexey-m.ru/articles/qip-infium-prodolzhenie-istorii 

## File structure ##

### File header ###

Header includes the history file info. From there, you can find out the size of the story (it is not clear why), the 
number of messages (it is not clear why it goes twice), the UIN and the nickname of the person. All numeric values 
are stored as `Big Endian`. Strings are stored in `UTF8`.

Position | HEX | Size | Description
------------- | ------------- | ------------- | -------------
0 | 0x00 | Char(3) | Signature. Always "QHF".
4 | 0x04 | Int32 | History size in bytes.
34 | 0x22 | Int32 | Number of messages.
38 | 0x26 | Int32 | ???
44 | 0x2C | Int16 | UIN length.
46 | 0x2E | Char(N) | UIN. N = length.
46 + N | 0x2E + N | Int16 | Nickname length.
48 + N | 0x30 + N | Char(N) | Nickname. N = length.

### Messages ###
Messages are right after the header. They are going one by one. The position is counted after the heading.

_Comment of serg987: analysis of ~ 50K messages shows that the values of most of the fields are always the 
same (see the "Value" column)_ 

Position | HEX | Size | Description | _Value_
------------- | ------------- | ------------- | -------------  | -------------
00 | 0x00 | Int16 | Signature. Always 1. | 1
02 | 0x02 | Int32 | Size of the message block. (w/o first 6 bytes)
06 | 0x06 | Int16 | Type of (?) message ID field. | 1
08 | 0x08 | Int16 | Size of the block with message ID. | 4
10 | 0x0A | Int32 | Message ID. Just a serial number. 
14 | 0x0E | Int16 | Type of (?) message sending date field. | 2
16 | 0x10 | Int16 | Size of message sending date field. | 4
18 | 0x12 | Int32 | Date UNIX format.
22 | 0x16 | Int16 | Type of field ??? | 3
24 | 0x18 | Int16 | ??? | 3
26 | 0x1A | Byte | Is the message sent. Boolean.
27 | 0x1B | Int16 | Type of message text field. | see table below
29 | 0x1D | Int16 | Size of the field of message length value. _(1)_ | 4
31 | 0x1F | Int16/32 _(1)_ | Message length in bytes.
35 | 0x23 | Byte(N) | Message text, _(non)_ encrypted. _(2)_

_Comment of serg987:_

_(1) - For QIP PDA files with non-encrypted message text Int16 (2 bytes) are used for length, but the value of the field 
0x1D - still equals 04. It may be a bug or maybe it is not a field size, but just another constant._

_(2) - For QIP PDA files message text is not encrypted._

### Message encryption ###
_Comment of serg987: not applicable for QIP PDA_

The text of the messages is stored in UTF8 encoding, as mentioned earlier. However, before converting the bytes to 
text, they are needed to be decrypted. The decrypting of the bytes of the message text is performed by the following 
principle:
```csharp
for (int i = 0; i < array.Length; i++)
    array[i] = (byte)(255 - array[i] - i - 1);
```

### Detailed field description ###
Each block is preceded by a block identifier of a 16-bit signed integer (or it is unsigned?). Here is a table below 
for the possible types of blocks with some details.

_Comment of serg987: according to observations, fields of type 05, 13, and 14 are duplicated by a corresponding phrase 
in the message text_

Type | Value
------------- | -------------
01 | Online message.
02 | Message sending date.
03 | Message sender.
05 | Authorization request.
06 | Friend request.
13 | Offline message.
14 | Authorization request accepted.
| | Added by serg987:
80 | QIP/ICQ service messages. Usually for connection issues.
81 | QIP/ICQ service messages. Usually for contacts' birthdays.

### License ###

Standard MIT license
Copyright (c) 2021 Sergey Kiselev

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the 
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the 
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

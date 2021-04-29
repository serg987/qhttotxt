Changelog:
ver 1.0a:
  - Added parsing and combining for QIP PDA (.cl) and Mchat (.cbd) files (works with -c key only). Combined file is saved
in working directory, and the data from contact lists is used for creating chat filenames and chat header.
  - Added parsing and combining of .txt files for Mchat, QIP-ICQ (exported?) and RNQ (exported with RnQHistoryReader.exe)
chats. Messages taken from txt files are combined with QHF and AHF so the whole history can be saved in a single file.
  - Charset of files is determined before storing it for better consistency. But it is still needed to pass it in 
commandline for machines without language of input files.
  - as the author achieved all the aims with this functionality, no other developing is planned unless there are bugs
or really important features to implement. 

ver 0.9a:
- First public release
- Parsing and combining QHF and AHF files
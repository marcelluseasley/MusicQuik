# MusiQuik - MP3 Song Downloader and Music Player #

**MusiQuik** is an Android app that can be used to search and download virtually any song ever for FREE, as long as it is royalty-free music.


## About MusiQuik ##
**MusiQuik** accesses a web service via a POST request, providing a song or artist search term. This search term is submitted to various MP3 music servers. If a match is found, a list of the relevant songs are returned, along with the direct URL of the MP3.
**MusiQuik** then proceeds to download the song via the Android DownloadManager, which handles failed/paused downloads and retries.
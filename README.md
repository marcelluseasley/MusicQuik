# MusiQuik - MP3 Song Downloader and Music Player #

**MusiQuik** is an Android app that can be used to search and download virtually any song ever for FREE, as long as it is royalty-free music.


## About MusiQuik ##
**MusiQuik** accesses a web service via a POST request, providing a song or artist search term. This search term is submitted to various MP3 music servers. If a match is found, a list of the relevant songs are returned, along with the direct URL of the MP3.
**MusiQuik** then proceeds to download the song via the Android DownloadManager, which handles failed/paused downloads and retries.

Initially, the scraping logic was written in Java and contained within the app itself. Since MP3 sites come and go, this would require an app update each time I made a change to correct any "broken" logic. Because of this fact, I moved the logic to the Python/Flask app (also available in the repository section of my Github). That way if the scrape logic breaks, I can just change the Python code to fix, and not disrupt any current owner's of the app. <br />
<img src="http://i.imgur.com/bVqjTXc.png" width="200" />
<img src="http://i.imgur.com/Mls33lV.png" width="200" />
<img src="http://i.imgur.com/YG6JrNf.png" width="200" />

## Updates - Amazon App Store (app still available on Aptoide.com)
Since **MusiQuik** is an app to download MP3s, Amazon found that it violated certain terms and conditions (i.e. DMCA), so it was pulled from the store. The total number of downloads before being pulled was **831** :
![downloads](http://i.imgur.com/oEDJt4V.png)
![graph](http://i.imgur.com/MU3ql5d.png)

Here is the lifeline of the app. :-( I created it as a proof of concept to show how web scraping can be used in an actual application.<br />
![lifeline](http://i.imgur.com/wKbQIRm.png)

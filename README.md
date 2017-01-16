# FlickrBuddy

A simple little program to pull down all of your Flickr photos to disk. Useful for backing up or migrating. Uses Flickr's API, which preserves photo metadata, including title/description changes you've made inside Flickr itself.

Usage: FlickrBuddy [local root to download to]

When run for the first time, it will use oAuth to get your Flickr credentials. Copy and paste the URL it gives into a browser, log yourself into Flickr and authorize FlickrBuddy, then copy the special code it gives you into your console window and hit Enter. FlickrBuddy will save the oAuth for future usage. If you encounter any problems, you can delete the 'credentials' file it creates and try again.

FlickrBuddy crawls your entire Flickr archive, going hierarchically: Collection / Album / Photo. Only photos in an Album, and Albums in a Collection, are retrieved. If you don't use Albums and Collections, Flickr's Batch Organize makes it easy enough to just put all of your album-less photos into a new Album, and to put all of your Albums into a new Collection.


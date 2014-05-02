#!/usr/bin/bash

# generate logo PNGs
convert -resize 512 -background transparent Logo.svg ../VineyardAndroidClient/ic_launcher-web.png
convert -resize 64  -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-mdpi/ic_launcher.png
convert -resize 128  -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-hdpi/ic_launcher.png
convert -resize 256  -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-xhdpi/ic_launcher.png
convert -resize 512 -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-xxhdpi/ic_launcher.png

# generate background PNGs
convert -resize 640  Background.png ../VineyardAndroidClient/res/drawable-mdpi/background.png
convert -resize 960  Background.png ../VineyardAndroidClient/res/drawable-hdpi/background.png
convert -resize 1024  Background.png ../VineyardAndroidClient/res/drawable-xhdpi/background.png
convert -resize 1024 Background.png ../VineyardAndroidClient/res/drawable-xxhdpi/background.png

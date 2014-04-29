#!/usr/bin/bash

# generate logo PNGs
convert -resize 512 -background transparent Logo.svg ../VineyardAndroidClient/ic_launcher-web.png
convert -resize 64  -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-mdpi/ic_launcher.png
convert -resize 128  -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-hdpi/ic_launcher.png
convert -resize 256  -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-xhdpi/ic_launcher.png
convert -resize 512 -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-xxhdpi/ic_launcher.png

# generate background PNGs
# convert -resize 512 -background transparent Logo.svg ../VineyardAndroidClient/ic_launcher-web.png
# convert -resize 48  -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-mdpi/ic_launcher.png
# convert -resize 72  -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-hdpi/ic_launcher.png
# convert -resize 96  -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-xhdpi/ic_launcher.png
# convert -resize 144 -background transparent Logo.svg ../VineyardAndroidClient/res/drawable-xxhdpi/ic_launcher.png

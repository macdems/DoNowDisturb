#!/bin/sh

make_icons() {
    inkscape -C -e ../app/src/main/res/mipmap-$2/ic_launcher.png -w $1 -h $1 ic_launcher.svg
    inkscape -C -e ../app/src/main/res/mipmap-$2/ic_launcher_round.png -w $1 -h $1 ic_launcher_round.svg
}

make_icons 36 ldpi
make_icons 48 mdpi
make_icons 72 hdpi
make_icons 96 xhdpi
make_icons 144 xxhdpi
make_icons 192 xxxhdpi

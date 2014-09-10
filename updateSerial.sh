su
mount -o remount,rw /system
cd  /sys/class/android_usb/android0/
rm iSerial
mv /sdcard/iSerial /sys/class/android_usb/android0/

## Welcome to version 2.0
This is the almost completely redone version of the android app.  It does not yet include user sign in, and I am working on moving async downloads 
to a loading screen at the begining with nice, scrolling images.  However, the home screen, calendar, and announcements are reworked completely, especially 
the calendar.  Lunch menu is also fixed, but currently using an external library.  I would love to get the pdf viewing using native libraries. Once these things are done, 2.0 will be ready for release!

## Neeeeewwwwww BUG
The navigation drawer item selection is suuuppppeeeerr buggy right now.  It's too late for me to finish fixing it though tonight, because I think it might need an in depth tracking to figure 
out the logic of the switches.  Yay.

## AndroidFLHSApp Directory Organization
This is the source code for the unpackaged Android App.
In the app folder...
The res directory (resources) contains the XML and really all other media for the APK besides the java class files.
Values, Layouts, and Menus are all stored there and written in XML.
The res--dpi folders are for storing all the drawables (PNG's, JPG's, GIF's). These are all the pictures!
The java folder stores all the class files. These are the actual programs in the app. 
The main package is com.flhs. This package contains another package, com.flhs.utils. This package has all the utility classes
that were were made for the classes in com.flhs.
The com.flhs classes are the Activities. Each activity is a seperate section of the app. To learn about activities.
To learn more about how this all works, go to the Android Developer Website and start learning: http://developer.android.com/training/index.html

## How to use Firebase
Its very easy to use Firebase.

## Current status
Right now, the goal is to create a way for students to sign in to the library easily. This is being accomplished by having the student sign in
with their google account to the app. The student will sign in when they first download the app and their user information will be populated. 
The user_card branch represents the bleeding edge of developement on this front. Other than this, we are also looking to interface the schedule 
feature with parent portal to automatically populate a student's schedule without them entering it, and general improvements.

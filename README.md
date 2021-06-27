[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)
## WorkManager
WorkManager is an usefull job scheduler to integrate long running and repeated task without any hassle. WorkManager is introduced by Google in 2019. In this project we use workmanager to create image uploader request in background with the help of retrofit and trigger progress notification to manage request.

![Using WorkManager](https://raw.githubusercontent.com/profahad/workmanager/master/screenplay/Screen%20Recording%202021-06-27%20at%203.40.46%20PM.gif)      ![Using Simple Retrofit](https://raw.githubusercontent.com/profahad/workmanager/master/screenplay/Screen%20Recording%202021-06-27%20at%203.46.29%20PM.gif)

## Features

- **WorkManager** **OneTimeWorkRequest** and **PeriodicWorkRequest** Requests
- Each WorkManager request handle with **Coroutines**
- **Retrofit Multipart** request with uploading progress
- Ongoing Notification to show and manage progress

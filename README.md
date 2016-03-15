# Android Boilerplate

Sample Android app that I use as a reference for my new Android projects. It demonstrates the architecture, tools and guidelines that I use when developing for the Android platform:

Libraries and tools included:

- Support Library
- Recycleview & Cardview
- Material design support
- Palette support
- [Retrolambda](https://github.com/evant/gradle-retrolambda)
- [Retrofit2](http://square.github.io/retrofit/)
- [Picasso](http://square.github.io/picasso/)
- [ButterKnife](http://jakewharton.github.io/butterknife/)

## Requirements
- Android SDK.
- Android 6.0 (API 23) .
- Latest Android SDK Tools and build tools.

## Architecture
This project follows Android architecture guidelines that are based on MVP (Model View Presenter). Read more about them [here](http://www.tinmegali.com/en/model-view-presenter-android-part-1/).

## Project structure
```
new-structure
├─ library-foobar
├─ app
│  ├─ libs
│  ├─ src
│  │  ├─ androidTest
│  │  │  └─ java
│  │  │     └─ com/abderrazak/recycleviewcardview
      ├─ test
│  │  │  └─ java
│  │  │     └─ com/abderrazak/recycleviewcardview
│  │  └─ main
│  │     ├─ java
│  │     │  └─ com/abderrazak/recycleviewcardview
│  │     ├─ res
│  │     └─ AndroidManifest.xml
│  ├─ build.gradle
│  └─ proguard-rules.pro
├─ build.gradle
└─ settings.gradle
```
## Java packages architecture

```
abderrazak.com.recycleviewcardview
├─ data
    ├─ local
    ├─ model
    ├─ remote
├─ network
    ├─ callbacks
    ├─ services
    ├─ tasks
├─ managers
├─ ui
│   ├─ main
    ├─ detail
    ├─ etc..
├─ util
└─ views
   ├─ adapters
   ├─ widgets
   └─ callbacks
```
### How to implement a new screen following MVP

Imagine you have to implement a main screen.

1. Create a new package under ```ui``` called main.

2. Create an new Activity called ```MainActivity```. You could also use a Fragment.

3. Define the view interface that your Activity is going to implement. Create a new interface called MainView. Add the methods that you think will be necessary, ```e.g. showDialog()```

4. Create a MainPresenterImpl class that implement MainPresenter.Implement the methods in MainPresenter that your Activity requires to perform the necessary actions, ```e.g. signIn(String email)```.

5. Create a SignInPresenterTestand write unit tests for signIn(email). Remember to mock the SignInMvpView and also the DataManager.

6. Make your MainActivity implement SignInMvpView and implement the required methods like showDialog()
In your activity, inject a new instance of MAinPresenter and call ``` presenter = new MainPresenterImpl(this) ``` from onCreate and ``` presenter.onDestroy() ``` from onDestroy(). Also, set up a click listener in your button that calls presenter.sortList().

### Things that experience made me learn the hard way

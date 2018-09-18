
Activity and Navigation Flow
- Entry point is Login Activity, users can register or login, Json Web Token received and saved to prefs
- Moves to Main Activity, starts at Home Fragment
- 5 main navigation tabs (copied from Instagram) :
    - Home Fragment
    - Discover Fragment
    - Upload Photo Activity
    - Activity Fragment
    - Profile Fragment
- Main Activity coordinates changing between each fragment when navigation tab is pressed
- Each main tab fragment has its own child fragment backstack, don't think will ever need to move between fragments if nav bar is not pressed
- Each main tab fragment only exists once in overall backstack

- Example :
    - going forward :
    - Home/FeedFrag -> Home/FragB --(press navbar)--> Discover/FragA -> Discover/FragB ---> Profile/FragA ---> Discover/FragB
    - going back :
    - Discover/FragB -> Discover/FragA -> Profile/FragA -> Home/FragB -> Home/FeedFrag


Making API calls
- Just call methods in InstaApi.java which take various parameters + a Retrofit Callback<String> object (because they are all asynchronous)
- 2 ways to deal with this :
    1. Pass in your own Retrofit Callback<String> implementation, but then you need to parse the String to JSONObjects and extract success/failure etc
    2. Use InstaApi.generateCallback to generate a Callback<String> object for you, which you then pass to the InstaApi methods
        a. InstaApi.generateCallback takes a context object and your concrete implementation of InstaApiCallback
        b. InstaApiCallback only needs you to implement the success method, but you can also override failure and networkFailure methods
        c. success is called when we receive a JSON reply from the server and it contains success : true, failure is called when it contains success : false, networkFailure is called when something goes wrong
        d. success and failure methods both pass you a JSONObject received from the server, which you can then process however you want


Getting Location
- Call MyApplication.getInstance().getLocation()
- This takes an activity context as well as a LocationCallback object (implement execute and permissionFailed methods)
- If permissions are not yet granted, a permission popup will appear
- The reason we need callbacks is that permissions are granted asynchronously
- If user allows, the execute method on your callback is called
- If user denies, the permissionFailed method is called
- If user also checks the never ask again button, this choice is stored in Prefs.LOCATION_DENIED_FOREVER as true (which you can check when permissionFailed is called)
    - in this scenario, it becomes a bit complicated if they change their mind and want to grant permission, because we have to navigate them to system settings


Preferences
- Access by calling Prefs.getInstance().read____ or .write_____, also need to supply a fallback if reading in case the key doesn't yet exist
- currently there are :
    - LOCATION_DENIED_FOREVER (boolean)
    - FEED_SORT (String)
    - JWT (String)


Utility Classes
- AspectImageView allows you to call setAspectRatio(ratio) where ratio is height/width
- To load images into any ImageView, call Glide.with(context).load(image).into(imageview)


Adapter
- FeedAdapter is used for RecyclerView
- It takes an ArrayList of Photo objects
- It supports both LinearLayout and GridLayout, all you have to do is call recyclerview.setLayoutManager(llm or glm) on the recyclerview the adapter is attached to
- Can set a header using adapter.setHeader (think of the Profile tab in the official Instagram app - the top part scrolls with the photo list)


Miscellaneous Utility Methods
- can call static methods from Utils class by calling Utils.Companion.xx
- photosFromJsonArray works to format a json array of photo objects into Photo objects (look at the json format in /feed in the API spec)
- usersFromJsonArray works similarly, as well as comments and likes
- formatDistance formats a Double in meters to a String representation, in m for distances <1km and in km for distances >= 1 km


TODO list :
1. Change API to return more details (display name etc on login)
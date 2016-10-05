# FRESH!
FRESH! is an Android app that help you to record food expiration date and send notifications to remind you when food is about to expired.

# User Guide
## Launch the app
There are three tabs in **main page**.
- `FRESH` - Unused, unexpired food will be listed here
- `USED` - All used food, regardless expired or not, will be listed here
- `EXPIRED` - Unsed, expired food will be listed here

All saved items will be listed in the main page by **expired date** in **descending** order.

##### Portrait & Landscape mode
![screenshot of main page in portrait mode]
(https://github.com/moisellegyg/reminder/blob/master/screenshots/ss_main2.png)
![screenshot of main page in portrait mode]
(https://github.com/moisellegyg/reminder/blob/master/screenshots/ss_main3.png)

## Create a new item
Press the **yellow button** at right bottom of main page to create a new item, which will navigate user to an **edit page**.

##### Portrait & Landscape mode
![screenshot of edit page]
(https://github.com/moisellegyg/reminder/blob/master/screenshots/ss_edit.png)
![screenshot of edit page landscape]
(https://github.com/moisellegyg/reminder/blob/master/screenshots/ss_edit_land.png)

There are five information items in the edit view.
- `Product image` - Optional input from user, press this will launch the camera for user to take a photo.
- `Product name` - Required input from user
- `Created date` - Automatically filled by system, the date on which this item is created.
- `Expired date` - Required input from user, the date on which this item will be expired.
- `Already used checkbox` - Optional input from user, check to mark this item is used by user.

Press `Save` to save this item, or `Cancel` to return back to main page.

When an item is saved, an alarm to send out a notification three days before the expiration date will be set. If the item is expired within three days, a notification will be sent out right after the item is being saved.

## Update an existing item
By selecting an item in the main page, user will be navigated to the **edit page** of the selected item. You can update the information or press to expand the menu button to delete this item from the database.

![screenshot of edit page]
(https://github.com/moisellegyg/reminder/blob/master/screenshots/ss_delete_single.png)

User can also **long press** to activate the multi-select mode in main page to do bulk delete.

![screenshot of main page in bulk delete mode]
(https://github.com/moisellegyg/reminder/blob/master/screenshots/ss_delete_bulk.png)

## Send a notification

There are three actions can be taken when seeing a notifcation.
- Press notificatoin card - This will laucn the edit page of the notified item.
- `USED` - Press this button to mark the item was used already. Notification will be dismissed and the item will be moved under `USED` tab in main page.
- `REMIND LATER` - Press this button to dismiss the notification. An alarm will be set to notify the user later. If the item will be expired tody, a new notification will be sent out after about three hours. Otherwise, the next day.

![screenshot of notification]
(https://github.com/moisellegyg/reminder/blob/master/screenshots/ss_notif.png)


# Development Notes
There are four main components in this app.
- Product main view
 - Used `RecyclerView` for listing items.
 - Defined `MultiSelectionState` for multi-select purpose since Android doesn't have one for `RecyclerView`.
 - `TransitionAnimation` is used for product image when navigating between main view and edit view.

- Product edit view
 - Enabled the **Delete** action in menu only when the edit view is for an existing item.
 - Defined customized `TextInputEditText` class `DatePickEditText` for inputting date. A `DatePickerDialog` will be launched when pressing a `DatePickEditText` instance.
 - Set `android:imeOptions="actionNext"` for product name so user can have a smoothier experience when typing inputs.

- Local database
  - Used SQLite database to save the local data. A URI(or path) of product image is stored in the database and the image itself is saved under `Environment.DIRECTORY_PICTURES` instead of writing the whole image as `BLOB` in the database.
  - Defined a customized `ContentProvider` class `ProductProvider` for quering data.
  - Defined a customized `AsyncQueryHandler` class `ProductQueryHandler` for quering data.
  - Implemented `LoaderManager.LoaderCallbacks<Cursor>` in `ProductListFragment` to load data in main page.
  
- Alarm & notificatoin
  - An alarm will be set via `AlarmService` everytime when user saves an item.
  - `AlarmBroadcastReceiver` will receive a broadcast and create a notification when alarm is triggered.
  - `NotificationBroadcastReceiver` will receive a broadcast when user takes action on a notification.

# References
- https://bignerdranch.github.io/recyclerview-multiselect/
- https://medium.com/@etiennelawlor/layout-tips-for-pre-and-post-lollipop-bcb2e4cdd6b2#.r9yh69ghg
- https://gist.github.com/skyfishjy/443b7448f59be978bc59
- https://www.reddit.com/r/androiddev/comments/37gocz/what_is_the_proper_way_to_use_a/
- http://stackoverflow.com/questions/31697083/recycler-view-add-remove-item-animations-not-showing-in-swapcursor
- http://stackoverflow.com/questions/14066038/why-image-captured-using-camera-intent-gets-rotated-on-some-devices-in-android

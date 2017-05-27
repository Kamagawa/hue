## Inspiration
Well, I ran traffic lights 3 times and almost got hit by car this week, and this actually happened quite often because it was hard for me to distinguish the color of the traffic light, so I decided to build something that will help me and countless other colorblind see the color. 

## What it does
display the world in your phone camera, and returns the color code and name of the object that you pointed to. 

## How I built it
I developed it on Mobile platform, because it is practical for people to use their phone to distinguish the color they run into in their lives. I chose Android because most people [88% uses android, while only 12% uses IOS worldwide]
 (https://qz.com/826672/android-goog-just-hit-a-record-88-market-share-of-all-smartphones/). In addition, I am better at Java than swift. XD

### Resouce::Data 
The first step was to prepare the data, which is quite easy, I went to an online color dictionary at colorhexa.com that maps hex code to rgb code and to English color names. I added this dictionary into a list, and in that list, each color is stored as a distinct ColorName data type. so when camera points to a location, it extract the color from the dictionary for color names.

### Service::Camera
This is the complicated part. In the beginning I thought I could just rely on the Android's system Camera without have to do much customization. But later I discovered, it was all customization! 
(1)First, a surface is drawn for camera to operate. 
(2)Then, envoke Frame, which automatically refreshes on every camera position change, and on every user input (put the phone to landscape etc). 

### Model::Algorithm
(3) Meanwhile the Frame is constantly computing the average color on the center Point of Interest of Camera in realtime through YUV data pipelines to RGB value and eventually to HEX.

### ViewController::Display
As soon as the calculation is done, the data is sent to the view class, where it display the eventual outcome at the button of the screen. The top and middle view is the camera view where user can pick other points of interest for new color.
 


## Challenges I ran into
This "Simple task" was actually A LOT of work. But aside from the tedius debugging process everything was sorted out except Camera. I was not able to make to auto zone to the optimal resolution, hence the camera view would seem a little bit blurry when you put the camera too close to the surface on which you wish to peek the color.

I should get the auto focus running so that the peek so it would appear to be more accurately extracting color data. It is actually the same accuracy considering the fact that the algorithm is calculating the average of the center block rather than the center point. So even if the picture become more clear, the underlying algorithm is the same. The reason why I chose not to pick a point is because a point may deviates from the surface' true color. But a clearer image would significantly increase the user experience.


## Accomplishments that I'm proud of
1. Was able to get the code working!
2. Handle the camera positioning, framing, and all those shenanigan myself without using an external library, which means much faster and more resource efficient than running a gigantic camera handler thread on the side
3. Had time left to implement the Restful call for learning more about the color. 
4. First time trying solo hackathon. 


## What I learned
I think I learned I've learned from previous hackathons when I could not even finish the project by the end. that is do not get right into coding but: 

1. Think about what you wanna make 

2. Plan and draw out the software architecture.
Things become so nice that I do not have to worry about reorganizing the code after a new module is donee)

3. Trim down your ambition. 
I originally wanted to do a AR model that guides people on safest path on campus, but it was not within the scope of a 1 person team in a 1 day hackathon

4. Do some research 
I learned [YUV color process](http://stackoverflow.com/a/10125048), 
Camera positioning from [cameraUtils](https://github.com/ralfgehrer/AndroidCameraUtil)
and camera positioning [optimization](https://github.com/ZBar/ZBar)

5. Get coding, and don't stop (if you pause and get food, you slag off)



## What's next for peeker
I will implement the AR function so that not only colorblind can use it for color, but this will transform to a life/travel/campus AR guide for people, which will be more useful and target a greater number of audience.



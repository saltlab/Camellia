function getPosition(theElement)
{
    var positionX = 0;
    positionX = getMe("sfu");
    helloWord(positionX);
    document.getElementById("foo").target = "ubc";

    while (theElement != null)
    {
        theElement = theElement.offsetParent;
        theElement;
    }
    return [positionX, positionY];
};

imageObjects[currImage] = new Image();
imageObjects[currImage].onload = preloadImages;
imageObjects[currImage].src = imageArray[currImage] + "?" + Math.random();

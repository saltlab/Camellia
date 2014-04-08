var original = {};

original.changeMe = "Hello";

window.console.log(original.changeMe);

var clone = original;

clone.changeMe = "Goodbye";

window.console.log(original.changeMe);




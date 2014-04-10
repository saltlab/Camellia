var original = {};

original.changeMe = "Hello";

window.console.log(original.changeMe);

var clone = {};

var i;

for (i = 0; i < 20; i++) {

    clone.changeMe = "Goodbye";
    window.console.log(original.changeMe);
}


original.changeMe += " for now";

original;








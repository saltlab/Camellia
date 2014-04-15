var base1 = {};

var assignMe = "bye";

base1.notSoHidden = {};

base1.howDoesThisWork = assignMe;

base1.getMyHidden = function () {
    return this.notSoHidden;
};


var clone1 = base1.notSoHidden;

clone1.newProp = "Hi";

window.console.log(base1.getMyHidden().newProp);

base1.getMyHidden().newProp = "Hello";

base1;

























































var base1 = {};

var assignMe = "bye";

base1.notSoHidden = {};

base1.howDoesThisWork = assignMe;

base1.getMyHidden = function () {
    return this.notSoHidden;
};


var clone1 = base1.getMyHidden();

clone1.newProp = "Hi";

window.console.log(base1.jetMyHidden(base1).newProp);

base1;



















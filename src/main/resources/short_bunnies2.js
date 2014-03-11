function Bar() {
    var privateAlso = "rumour";

    this.publicAlso = {
        a: 1,
        b: 2
    };

    this.getPrivate = function () {
        return privateAlso;
    };

    this.setPrivate = function (b) {
        privateAlso = b;
    };
}

function Foo() {

    var private = "secret";

    var privateComplex = new Bar();

    this.public = "announcement";

    this.getPrivate = function () {
        return private;
    };

    this.setPrivate = function (a) {
        private = a;
    };

    this.getPrivateComplex = function () {
        return privateComplex;
    };

    this.setPrivateComplex = function (a) {
        privateComplex = a;
    };
}


var tt = new Foo();

tt.newMethod = function () {
    return this.public;
};

var yy = tt.getPrivateComplex().publicAlso;

yy.a = 3;

tt;

var r = {
    prop1: "hello",
    prop2: "world"
};




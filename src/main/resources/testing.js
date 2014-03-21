var global = "hello world!";

function () {

    mutator(r, a, nested(r));

    var second = r;

    second.newP = "unexpected";

    second.third.method();

    r = "end";
}

var global = "hello world!";

function () {

    mutator(r, a, nested(r));

    var second = r;

    second.newP = "unexpected";

    r = "end";
}

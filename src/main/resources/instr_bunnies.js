var tt = _dynoWrite("tt", "hello", 0);

tt = _dynoWrite("tt", {
                        prop: "world!"
                      }, 2);

tt = _dynoWrite("tt", "goodbye", 6);

tt[_dynoReadProp("tt", "hello", 8)][_dynoReadProp("tt.hello", "goodbye", 8)];

var dd = _dynoWrite("dd", "tt", tt, 10);

dd = _dynoWrite("dd", tt, 12);

_dynoFunc('callMe', callMe, 0)(otherCall(), (tt[_dynoReadProp("tt", "prop1", 14)][_dynoReadProp("tt.prop1", "prop2", 14)])());



var tt = {};

tt.newProp = "Hi";

tt.newProp2 = {};

var vv = tt;

var xx = {};

xx.prop = vv;

xx.newProp = "what";

xx.prop.newProp = "what";

vv.newProp = "Hello";

vv = {};

vv.newProp = "Hello";

var zz = tt.newProp2;





















